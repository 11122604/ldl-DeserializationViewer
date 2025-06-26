package com.datalight.tools.deserialization.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.datalight.tools.deserialization.model.RedisOperParam;
import com.datalight.tools.deserialization.service.RedisDataService;
import com.datalight.tools.deserialization.core.SerialObjectInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 /**
 * @author 1053459255@qq.com
 * @since 2025-06-26
 */
@Service
public class RedisDataServiceImpl implements RedisDataService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RedisDataServiceImpl.class);

    @Value("${host.config.windows:C:\\Users\\hostconfig.txt}")
    private String hostConfigWindows;

    @Value("${host.config.linux:/etc/redisaliyun/hostconfig.txt}")
    private String hostConfigLinux;
    public static  String HOST_CONFIG_WINDOW  = "C:\\Users\\hostconfig.txt";

    public static  String HOST_CONFIG_LINUX  = "/etc/redisaliyun/hostconfig.txt";

    /**
     *
     *
     * @param redisOperParam@return
     */
    @Override
    public String searchByKey(RedisOperParam redisOperParam) throws Exception {
        return searchByKeySt(redisOperParam);
    }
    /**
     *
     * @param redisOperParam
     * @return
     * @throws Exception
     */
    public static String searchByKeySt(RedisOperParam redisOperParam) throws Exception {
        return queryRedis(redisOperParam);
    }
    /**
     *
     * @param redisOperParam
     */
    @Override
    public void putData(RedisOperParam redisOperParam) throws Exception {
        if(!redisOperParam.getIpAndPort().contains(",")){
            Jedis jedis = createJedis(redisOperParam.getIpAndPort(),redisOperParam.getPassword());
            jedis.set(redisOperParam.getKey().getBytes(), redisOperParam.getFileData().getBytes());
        }else {
            JedisCluster cluster = createjedisCluster(redisOperParam.getIpAndPort(),redisOperParam.getPassword());
            cluster.set(redisOperParam.getKey().getBytes(), redisOperParam.getFileData().getBytes());
        }
        return ;
    }
    /**
     *
     * @param redisOperParam
     */
    @Override
    public void putValue(RedisOperParam redisOperParam) {
        if(!redisOperParam.getIpAndPort().contains(",")){
            Jedis jedis = createJedis(redisOperParam.getIpAndPort(),redisOperParam.getPassword());
            jedis.set(redisOperParam.getKey(), redisOperParam.getValue());
        }else {
            JedisCluster cluster = createjedisCluster(redisOperParam.getIpAndPort(),redisOperParam.getPassword());
            cluster.set(redisOperParam.getKey(), redisOperParam.getValue());
        }
        return ;
    }
    /**
     *
     * @param redisOperParam
     * @return
     * @throws Exception
     */
    @Override
    public String  searchWithEnv(RedisOperParam redisOperParam) throws Exception {
        return searchWithEnvSt( redisOperParam);
    }

    /**
     *
     * @param redisOperParam
     * @return
     */
    public static String  searchWithEnvSt(RedisOperParam redisOperParam) throws Exception {
        String path = acquirePath();
        List<String> items = Files.readAllLines(Paths.get(path, new String[0]));
        if(items == null || items.size() == 0){
            logger.error("配置文件为空");
            throw new Exception("配置文件为空");
        }
        String addressPass = null;
        for(String item : items){
            if(redisOperParam.getEnvName().equals(item.split("#")[0])){
                addressPass = item.split("#")[1];
                break ;
            }
        }
        if(StringUtils.isBlank(addressPass)){
            logger.error("配置项不存在");
            throw new Exception("配置项不存在");
        }
        RedisOperParam redisOpr = generParam(addressPass);
        redisOpr.setKey(redisOperParam.getKey());
        String result =  queryRedis(redisOpr);
        return result;
    }

    /**
     *
     * @param addressPass
     * @return
     */
    public static RedisOperParam generParam(String addressPass){
        String address = null;
        String password = null;
        if(addressPass.contains("@")){
            address = addressPass.split("@")[0];
            password = addressPass.split("@")[1];
        }else {
            address = addressPass;
        }
        RedisOperParam redisOpr = new RedisOperParam();
        redisOpr.setIpAndPort(address);
        if(StringUtils.isNotEmpty(password)){
            redisOpr.setPassword(password);
        }
        return redisOpr;
    }

    /**
     *
     * @param redisOperParam
     * @return
     */
    private static String queryRedis(RedisOperParam redisOperParam) {
        byte[] result = null;
        try {
            result =  queryData(redisOperParam);
        }catch (Exception ex){
            logger.error("查询redis失败, key: {}, ipAndPort: {}", redisOperParam.getKey(), redisOperParam.getIpAndPort(), ex);
            throw ex;
        }
        if(result == null || result.length == 0){
            return null;
        }
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(result);
            SerialObjectInputStream ois = new SerialObjectInputStream(bis);
            Object o = ois.readObject();
            ois.close();
            return JSONObject.toJSONString(o);
        } catch (Exception e) {
            logger.warn("读取对象失败, 以默认字符集返回 key: {}, ipAndPort: {}", redisOperParam.getKey(), redisOperParam.getIpAndPort(), e);
            String str = new String(result, Charset.defaultCharset());
            return str;

        }
    }

    /**
     *
     * @param redisOperParam
     * @return
     */
    private  static byte[] queryData(RedisOperParam redisOperParam){
        byte[] result;
        if(!redisOperParam.getIpAndPort().contains(",")){
            Jedis jedis = createJedis(redisOperParam.getIpAndPort(),redisOperParam.getPassword());
            Set<byte[]> keys = jedis.keys(redisOperParam.getKey().getBytes());
            if(keys == null || keys.size() == 0){
                logger.error("key不存在: {}", redisOperParam.getKey());
                throw new RuntimeException("key不存在: " + redisOperParam.getKey());
            }
            result = jedis.get(redisOperParam.getKey().getBytes());
        }else {
            JedisCluster cluster = createjedisCluster(redisOperParam.getIpAndPort(),redisOperParam.getPassword());
            result = cluster.get(redisOperParam.getKey().getBytes());
        }
        return result;
    }

    /**
     *
     * @param address
     * @return
     */
    private static Jedis createJedis(String address,String password){
        String[] ipParams = address.split(":");
        Jedis jedis = new Jedis(ipParams[0], Integer.parseInt(ipParams[1]));
        if(StringUtils.isNotEmpty(password)){
            jedis.auth(password);
        }
        return jedis;
    }

    /**
     *
     * @param address
     * @return
     */
    private static JedisCluster createjedisCluster(String address,String password){
        String[] nodes = address.split(",");
        Set<HostAndPort> set = new HashSet<>(nodes.length);
        for (String node : nodes){
            if(StringUtils.isBlank(node)){
                continue;
            }
            String[] ipParams = node.split(":");
            set.add(new HostAndPort(ipParams[0], Integer.parseInt(ipParams[1])));
        }
        JedisCluster jedisCluster;
        if(StringUtils.isNotEmpty(password)){
             jedisCluster = new JedisCluster(set,1000,1000,5,password,new GenericObjectPoolConfig());
        }else {
             jedisCluster = new JedisCluster(set, 1000);
        }
        return jedisCluster;
    }

    public static String acquirePath(){
        String path = null;
        String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().startsWith("windows")) {
            logger.info("当前系统版本是: {}", os);
            path = HOST_CONFIG_WINDOW;
        } else { //其它操作系统
            logger.info("当前系统版本是: {}", os);
            path = HOST_CONFIG_LINUX;
        }
        return path;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        HOST_CONFIG_WINDOW = this.hostConfigWindows;
        HOST_CONFIG_LINUX = this.hostConfigLinux;
    }

}
