package com.datalight.tools.deserialization.util;

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
           System.out.println("启用自定义加载: " + desc.getName());
       }
        SerialClassLoaderJavassist serialClassLoader = new SerialClassLoaderJavassist();
        byte[] bytes = new byte[0];
        try {
            bytes = serialClassLoader.generate(desc);
        } catch (Exception e) {
            System.out.println("自定义加载失败" + desc.getName());
            //e.printStackTrace();
            throw new ClassNotFoundException("自定义加载失败" + desc.getName());
        }
        String className = desc.getName().replace("/",".");
        Class<?> returnClass=  serialClassLoader.defineClass(className,bytes);
        return returnClass;
    }


}
