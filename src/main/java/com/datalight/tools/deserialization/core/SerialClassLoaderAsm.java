package com.datalight.tools.deserialization.core;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.RETURN;
/**
 /**
 * @author 1053459255@qq.com
 * @since 2025-06-26
 */
public class SerialClassLoaderAsm extends ClassLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SerialClassLoaderAsm.class);
    
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
        byte[] bytes =  generateByAsm(objectStreamClass);
        return bytes;
    }

    /**
     *
     * @param objectStreamClass
     * @return
     */
    public byte[] generateByAsm(ObjectStreamClass objectStreamClass) throws Exception {
        String name = objectStreamClass.getName();

        String className = name.replace(".","/");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className,null, "java/lang/Object", new String[]{"java/io/Serializable"});
        
        // 添加serialVersionUID字段（正确的long类型，private static final）
        FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, 
                                       "serialVersionUID", "J", null, objectStreamClass.getSerialVersionUID());
        fv.visitEnd();
        
        // 添加默认构造函数
        MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
        ObjectStreamField[] fields = objectStreamClass.getFields();
        if(fields == null || fields.length == 0){
            cw.visitEnd();
            return cw.toByteArray();
        }
        for(ObjectStreamField field : fields){
            try {
                Method fm = field.getClass().getDeclaredMethod("getSignature",new Class[0]);
                fm.setAccessible(true);
                String typeOf = (String)fm.invoke(field,new Object[0]);
                String fieldName = field.getName();
                
                // 与Javassist保持一致的类型处理逻辑
                if((typeOf.startsWith("L") && !typeOf.startsWith("Ljava")) || typeOf.startsWith("[")){
                    typeOf = "Ljava/lang/Object;";
                }
                cw.visitField(ACC_PRIVATE, fieldName, typeOf, null, null).visitEnd();
                
                // 生成getter方法
                String getMethodName = "get" + StringUtils.capitalize(fieldName);
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getMethodName, "()" + typeOf, null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, typeOf);
                mv.visitInsn(getReturnOpcode(typeOf));
                mv.visitMaxs(0, 0); // COMPUTE_MAXS会自动计算
                mv.visitEnd();
                
                // 生成setter方法
                String setMethodName = "set" + StringUtils.capitalize(fieldName);
                mv = cw.visitMethod(ACC_PUBLIC, setMethodName, "(" + typeOf + ")V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(getLoadOpcode(typeOf), 1);
                mv.visitFieldInsn(PUTFIELD, className, fieldName, typeOf);
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0); // COMPUTE_MAXS会自动计算
                mv.visitEnd();
            } catch (Exception e) {
                logger.error("生成字段错误 name: {}", field.getName(), e);
            }

        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * 获取加载指令
     * @param typeDescriptor
     * @return
     */
    private int getLoadOpcode(String typeDescriptor) {
        if ("I".equals(typeDescriptor) || "Z".equals(typeDescriptor) || 
            "B".equals(typeDescriptor) || "C".equals(typeDescriptor) || 
            "S".equals(typeDescriptor)) {
            return ILOAD;
        } else if ("J".equals(typeDescriptor)) {
            return LLOAD;
        } else if ("D".equals(typeDescriptor)) {
            return DLOAD;
        } else if ("F".equals(typeDescriptor)) {
            return FLOAD;
        } else {
            return ALOAD;
        }
    }

    /**
     * 获取返回指令
     * @param typeDescriptor
     * @return
     */
    private int getReturnOpcode(String typeDescriptor) {
        if ("I".equals(typeDescriptor) || "Z".equals(typeDescriptor) || 
            "B".equals(typeDescriptor) || "C".equals(typeDescriptor) || 
            "S".equals(typeDescriptor)) {
            return IRETURN;
        } else if ("J".equals(typeDescriptor)) {
            return LRETURN;
        } else if ("D".equals(typeDescriptor)) {
            return DRETURN;
        } else if ("F".equals(typeDescriptor)) {
            return FRETURN;
        } else {
            return ARETURN;
        }
    }

    /**
     *
     * @param typeof
     * @return
     */
    private int[] loadAndReturnOf(String typeof) {
        if (typeof.equals("I") || typeof.equals("Z") || typeof.equals("B") || 
            typeof.equals("C") || typeof.equals("S")) {
            return new int[]{ILOAD, IRETURN};
        } else if (typeof.equals("J")) {
            return new int[]{LLOAD, LRETURN};
        } else if (typeof.equals("D")) {
            return new int[]{DLOAD, DRETURN};
        } else if (typeof.equals("F")) {
            return new int[]{FLOAD, FRETURN};
        } else {
            return new int[]{ALOAD, ARETURN};
        }
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
