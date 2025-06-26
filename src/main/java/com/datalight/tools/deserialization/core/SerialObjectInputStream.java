package com.datalight.tools.deserialization.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * @author leolu
 * @since 2022-10-13
 *
 */
public class SerialObjectInputStream extends ObjectInputStream {
    
    private static final Logger logger = LoggerFactory.getLogger(SerialObjectInputStream.class);

    public SerialObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected SerialObjectInputStream() throws IOException, SecurityException {
        super();
    }

    /**
     *
     * @param desc
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected Class<?> resolveClass(ObjectStreamClass desc)  throws IOException, ClassNotFoundException{
       try{
           return super.resolveClass(desc);
       }catch (Exception ex){
           logger.info("启用自定义加载: {}", desc.getName());
       }
        // 优先尝试Javassist实现
        SerialClassLoaderJavassist javassistLoader = new SerialClassLoaderJavassist();
        byte[] bytes = new byte[0];
        try {
            bytes = javassistLoader.generate(desc);
            String className = desc.getName().replace("/",".");
            Class<?> returnClass = javassistLoader.defineClass(className, bytes);
            return returnClass;
        } catch (Exception e) {
            logger.warn("Javassist加载失败，尝试ASM: {}", desc.getName());
        }
        
        // 备用方案：尝试ASM实现
        SerialClassLoaderAsm asmLoader = new SerialClassLoaderAsm();
        try {
            bytes = asmLoader.generate(desc);
            String className = desc.getName().replace("/",".");
            Class<?> returnClass = asmLoader.defineClass(className, bytes);
            logger.info("ASM加载成功: {}", desc.getName());
            return returnClass;
        } catch (Exception e) {
            logger.error("ASM加载也失败: {}", desc.getName(), e);
            throw new ClassNotFoundException("所有自定义加载方式均失败: " + desc.getName());
        }
    }


}
