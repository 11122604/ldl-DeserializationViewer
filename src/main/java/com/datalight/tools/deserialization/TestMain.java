package com.datalight.tools.deserialization;

import com.alibaba.fastjson.JSONObject;
import com.datalight.tools.deserialization.model.TestDto;
import com.datalight.tools.deserialization.util.SerialObjectInputStream;

import java.io.*;

public class TestMain {
    public static void main(String[] args) throws Exception {
        TestDto testDto = new TestDto();
        testDto.setId(1L);
        testDto.setName("姓名");
        testDto.setOtherInfo("学生");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(testDto);
        oos.close();
        System.out.println("转义前：");
        System.out.println(new java.lang.String(baos.toByteArray()));
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        SerialObjectInputStream ois = new SerialObjectInputStream(bis);
        Object o = ois.readObject();
        ois.close();
        System.out.println("转义后：");
        System.out.println(JSONObject.toJSONString(o));
    }
}
