package com.datalight.tools.deserialization;

import com.alibaba.fastjson.JSON;
import com.datalight.tools.deserialization.model.RedisOperParam;
import com.datalight.tools.deserialization.service.impl.RedisDataServiceImpl;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * @author leolu
 * @since 2022-10-13
 */
public class DeserializationViewerApplication {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("参数：" + JSON.toJSON(args));
        if(args.length != 2){
            System.out.println("参数格式应为[ip:port rediskey] 或 [配置文件数字 rediskey]");
            return ;
        }
        ResourceBundle resource = ResourceBundle.getBundle("config/application");
        try {
            RedisDataServiceImpl.HOST_CONFIG_LINUX =  resource.getString("host.config.linux");
            RedisDataServiceImpl.HOST_CONFIG_WINDOW = resource.getString("host.config.windows");
        }catch (Exception e){
            System.out.println("环境配置不全");
        }
        String argTwo = args[1];
        String argOne = args[0];
        String argOnePrifix = argOne.split(":")[0];
        Pattern pattern = Pattern.compile("(([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])");
        String str = null;
        try{
            if(pattern.matcher(argOnePrifix).matches()){
                RedisOperParam redisOpr = RedisDataServiceImpl.generParam(argOne);
                redisOpr.setKey(argTwo);
                str = RedisDataServiceImpl.searchByKeySt(redisOpr);
            }else {
                RedisOperParam param = new RedisOperParam();
                param.setEnvName(argOne);
                param.setKey(argTwo);
                str = RedisDataServiceImpl.searchWithEnvSt(param);
            }
        } catch (IOException e) {
            System.out.println("配文件读取失败");
            e.printStackTrace();
            return ;
        }catch (Exception e){
            System.out.println("查询失败");
            e.printStackTrace();
            return ;
        }
        if(str == null){
            System.out.println("----------> not found");
            return;
        }else {
            System.out.println(str);
        }

    }




}
