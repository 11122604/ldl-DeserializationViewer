package com.datalight.tools.deserialization;

import com.alibaba.fastjson.JSONObject;
import com.datalight.tools.deserialization.model.TestDto;
import com.datalight.tools.deserialization.core.SerialObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class TestMain {
    
    private static final Logger logger = LoggerFactory.getLogger(TestMain.class);
    
    public static void main(String[] args) throws Exception {
        TestDto testDto = new TestDto();
        testDto.setId(1L);
        testDto.setName("姓名");
        testDto.setOtherInfo("学生");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(testDto);
        oos.close();
        logger.info("转义前：");
        logger.info("原始字节: {}", new java.lang.String(baos.toByteArray()));
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        SerialObjectInputStream ois = new SerialObjectInputStream(bis);
        Object o = ois.readObject();
        ois.close();
        logger.info("转义后：");
        logger.info("JSON结果: {}", JSONObject.toJSONString(o));
    }
}
