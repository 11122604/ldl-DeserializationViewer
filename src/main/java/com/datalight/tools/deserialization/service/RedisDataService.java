package com.datalight.tools.deserialization.service;

import com.datalight.tools.deserialization.model.RedisOperParam;

/**
 *
 */
public interface RedisDataService {
    /**
     *
     *
     * @param redisOperParam@return
     */
    String searchByKey(RedisOperParam redisOperParam) throws Exception;

    /**
     *
     * @param redisOperParam
     */
    void putData(RedisOperParam redisOperParam) throws Exception;

    /**
     *
     * @param redisOperParam
     */
    void putValue(RedisOperParam redisOperParam);

    /**
     *
     * @param redisOperParam
     * @return
     */
    String searchWithEnv(RedisOperParam redisOperParam) throws Exception;
}
