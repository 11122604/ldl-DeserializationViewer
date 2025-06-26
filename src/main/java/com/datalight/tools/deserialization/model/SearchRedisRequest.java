package com.datalight.tools.deserialization.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SearchRedisRequest {

    @ApiModelProperty(value = "redis值的key",required = true)
    private String key;

    @ApiModelProperty(value = "redis值的ip和端口号",required = true)
    private String ipAndPort;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

}
