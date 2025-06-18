package com.datalight.tools.deserialization.util;

import javassist.*;
import org.apache.commons.lang.StringUtils;

import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Method;

/**
 * @author leolu
 * @since 2022-10-13
 */
public class SerialClassLoaderJavassist extends ClassLoader {
    /**
     *
     * @param name
     * @param b
     * @return
     */
    public Class<?> defineClass(String name, byte[] b) {
        // ClassLoader是个抽象类，而ClassLoader.defineClass 方法是protected的
        // 所以我们需要定义一个子类将这个方法暴露出来
        return super.defineClass(name, b, 0, b.length);
    }

    /**
     *
     * @param objectStreamClass
     * @return
     * @throws Exception
     */
    public byte[] generate(ObjectStreamClass objectStreamClass) throws Exception {
        byte[] bytes =  generateByAssit(objectStreamClass);
        return bytes;
    }

    /**
     *
     * @param objectStreamClass
     * @return
     * @throws Exception
     */
    public byte[] generateByAssit(ObjectStreamClass objectStreamClass) throws Exception {
        String className = objectStreamClass.getName();
       // ClassPool pool = ClassPool.getDefault();
        ClassPool pool = new ClassPool(null);
        pool.appendSystemPath();
        CtClass cc = pool.makeClass(className);
        cc.addInterface(pool.get("java.io.Serializable"));
        CtField staticField = CtField.make("private static final long serialVersionUID;",cc);
        cc.addField(staticField,CtField.Initializer.constant(objectStreamClass.getSerialVersionUID()));
        ObjectStreamField[] fields = objectStreamClass.getFields();
        if(fields == null || fields.length == 0){
            cc.toBytecode();
        }
        for(ObjectStreamField field : fields){
            Method fm = field.getClass().getDeclaredMethod("getSignature",new Class[0]);
            fm.setAccessible(true);
            String typeOf = (String)fm.invoke(field,new Object[0]);
            String fieldName = field.getName();
            if((typeOf.startsWith("L") && !typeOf.startsWith("Ljava")) || typeOf.startsWith("[")){
                typeOf = "Ljava/lang/Object;";
            }
            CtField ctField;
            if(typeOf.startsWith("L")){
                typeOf = typeOf.replaceFirst("L","").replace("/",".").replace(";","");
                ctField = new CtField(pool.get(typeOf),field.getName(),cc);
                ctField.setModifiers(Modifier.PRIVATE);
            }else {
                String primaryType = translatePrimary(typeOf);
                ctField = CtField.make("private " + primaryType  + " " + fieldName + ";",cc);
            }
            cc.addField(ctField);
            String getMethodName = "get" + StringUtils.capitalize(fieldName);
            String setMethodName = "set" + StringUtils.capitalize(fieldName);
            cc.addMethod(CtNewMethod.setter(setMethodName, ctField));
            cc.addMethod(CtNewMethod.getter(getMethodName, ctField));
        }
        return  cc.toBytecode();
    }

    /**
     *
     * @param typeOf
     * @return
     */
    private String translatePrimary(String typeOf) throws Exception {
        switch (typeOf) {
            case "Z":
                return "boolean";
            case "B":
                return "byte";
            case "C":
                return "char";
            case "S":
                return "short";
            case "I":
                return "int";
            case "F":
                return "float";
            case "J":
                return "long";
            case "D":
                return "double";
            default:
                throw new Exception("类型错误");
        }
    }

}
