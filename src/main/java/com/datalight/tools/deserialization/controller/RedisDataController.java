package com.datalight.tools.deserialization.controller;

import com.datalight.tools.deserialization.model.Result;
import com.datalight.tools.deserialization.model.RedisOperParam;
import com.datalight.tools.deserialization.service.RedisDataService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @author 1053459255@qq.com
 * @since 2022-10-13
 */
@RestController
public class RedisDataController {

    @Autowired
    RedisDataService redisDataService;


    @RequestMapping(path = "/search",method = RequestMethod.GET)
    @ApiOperation(value = "根据主机查询redis")
    public Result<String> searchByKey(@RequestParam(name = "ipAndPort",required = true) String ipAndPort,
                                      @RequestParam(name = "password",required = false) String password,
                                      @RequestParam(name = "key",required = true) String key){
        Result<String> result;
        try{
            RedisOperParam redisOperParam = new RedisOperParam();
            redisOperParam.setIpAndPort(ipAndPort);
            redisOperParam.setPassword(password);
            redisOperParam.setKey(key);
            String str = redisDataService.searchByKey(redisOperParam);
            result = new Result<>(str);
            if(str == null){
                result.setMessage(Arrays.asList("查询结果为空"));
            }
            return result;
        }catch (Exception ex){
            ex.printStackTrace();
            result = new Result<>();
            result.setSuccess(false);
            result.setMessage(Arrays.asList(ex.getMessage()));
        }
        return result;

    }

    /**
     *
     * @param
     * @param key
     * @return
     */
    @RequestMapping(path = "/searchByEnv",method = RequestMethod.GET)
    @ApiOperation(value = "根据环境配置查询redis")
    @ApiImplicitParam(name = "envName", paramType = "query", allowableValues = "DEV,TEST,PRODUCT,ENV1,ENV2",required = true)
        public Result<String> searchWithEnv(@RequestParam(name = "key",required = true) String key,String envName){
        Result<String> result;
        try{
            RedisOperParam redisOperParam = new RedisOperParam();
            redisOperParam.setKey(key);
            redisOperParam.setEnvName(envName);
            String str = redisDataService.searchWithEnv(redisOperParam);
            result = new Result<>(str);
            if(str == null){
                result.setMessage(Arrays.asList("查询结果为空"));
            }
            return result;
        }catch (Exception ex){
            ex.printStackTrace();
            result = new Result<>();
            result.setSuccess(false);
            result.setMessage(Arrays.asList(ex.getMessage()));
        }
        return result;

    }

}
