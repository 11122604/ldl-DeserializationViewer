package com.datalight.tools.deserialization.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class Result<TDATA> implements Serializable {

    private static final long serialVersionUID = 7395796485143585787L;

    private Boolean success = Boolean.TRUE;


    private String sys = "service.redisAliyun";

    private int code = 0;

    private Object message;

    @JsonInclude(Include.NON_NULL)
    private TDATA data;


    public Result() {
        this.code = 0;
        this.message = null;
        this.data = null;
    }


    public Result(TDATA data) {
        this.code = 0;
        this.message = null;
        this.data = data;
    }


    public Result(int code) {
        success = Boolean.FALSE;
        this.code = code;
        this.message = "Unknow Error";
    }


    public Result(int code, String message) {
        success = Boolean.FALSE;
        this.code = code;

        this.message = message;
    }

    public Result(int code, Map<String, String> messages) {
        success = Boolean.FALSE;
        this.code = code;

        this.message = messages;
    }

    public Result(int code, String message, TDATA data) {
        success = Boolean.FALSE;
        this.code = code;

        this.message = message;
        this.data = data;
    }

    public Result(int code, Map<String, String> messages, TDATA data) {
        success = Boolean.FALSE;
        this.code = code;
        this.message = messages;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Object getMessage() {
        return message;
    }

    public TDATA getData() {
        return data;
    }


    public Boolean getSuccess() {
        return success;
    }


    public void setSuccess(Boolean success) {
        this.success = success;
    }


    public String getSys() {
        return sys;
    }


    public void setSys(String sys) {
        this.sys = sys;
    }


    public void setCode(int code) {
        this.code = code;
    }


    public void setMessage(List<String> message) {
        this.message = message;
    }


    public void setData(TDATA data) {
        this.data = data;
    }
}
