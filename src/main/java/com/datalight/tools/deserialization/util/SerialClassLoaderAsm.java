package com.datalight.tools.deserialization.util;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.*;

import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.RETURN;

public class SerialClassLoaderAsm extends ClassLoader {
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
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className,null, "java/lang/Object", new String[]{"java/io/Serializable"});
        cw.visitField(ACC_PUBLIC + ACC_STATIC, "serialVersionUID", "I", null, null).visitEnd();
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
                if(typeOf.startsWith("L") && !typeOf.startsWith("Ljava")){
                    typeOf = "Ljava/lang/Object;";
                }
                cw.visitField(ACC_PRIVATE, fieldName, typeOf, null, null).visitEnd();
                // getMethod
                String getMethodName = "get" + StringUtils.capitalize(fieldName);
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getMethodName, "()" + typeOf, null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, typeOf);
                mv.visitInsn(loadAndReturnOf(typeOf)[1]);
                mv.visitMaxs(2, 1);
                mv.visitEnd();
                String setMethodName = "set" + StringUtils.capitalize(fieldName);
                // setMethod
                mv = cw.visitMethod(ACC_PUBLIC, setMethodName, "(" + typeOf + ")V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(loadAndReturnOf(typeOf)[0], 1);
                mv.visitFieldInsn(PUTFIELD, className, fieldName, typeOf);
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 3);
                mv.visitEnd();
            } catch (Exception e) {
                System.out.print("生成字段错误 name:" + field.getName());
                e.printStackTrace();
            }

        }
        cw.visitEnd();
        byte[] clbytes = cw.toByteArray();
        clbytes =  transform(clbytes);
        return clbytes;
    }

    /**
     *
     * @param typeof
     * @return
     */
    private int[] loadAndReturnOf(String typeof) {
        if (typeof.equals("I") || typeof.equals("Z")) {
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
     * @param origClassData
     * @return
     * @throws Exception
     */
    public static byte[] transform(byte[] origClassData) throws Exception {
        ClassReader cr = new ClassReader(origClassData);
        final ClassWriter cw = new ClassWriter(cr, Opcodes.ASM4);

        // initialize the above fields
        ClassVisitor cv = new ClassVisitor(ASM4, cw) {
            boolean visitedStaticBlock = false;
            class StaticBlockMethodVisitor extends MethodVisitor {
                StaticBlockMethodVisitor(MethodVisitor mv) {
                    super(ASM4, mv);
                }
                public void visitCode() {
                    super.visitCode();
                    super.visitInsn(ICONST_1); // pass argument 1 to constructor
                    super.visitFieldInsn(PUTSTATIC, "TestObj", "serialVersionUID", "I");

                }

                public void visitMaxs(int maxStack, int maxLocals) {
                    // The values 3 and 0 come from the fact that our instance
                    // creation uses 3 stack slots to construct the instances
                    // above and 0 local variables.
                    final int ourMaxStack = 3;
                    final int ourMaxLocals = 0;

                    // now, instead of just passing original or our own
                    // visitMaxs numbers to super, we instead calculate
                    // the maximum values for both.
                    super.visitMaxs(Math.max(ourMaxStack, maxStack), Math.max(ourMaxLocals, maxLocals));
                }
            }

            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (cv == null) {
                    return null;
                }
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if ("<clinit>".equals(name) && !visitedStaticBlock) {
                    visitedStaticBlock = true;
                    return new StaticBlockMethodVisitor(mv);
                } else {
                    return mv;
                }
            }

            public void visitEnd() {
                // All methods visited. If static block was not
                // encountered, add a new one.
                if (!visitedStaticBlock) {
                    // Create an empty static block and let our method
                    // visitor modify it the same way it modifies an
                    // existing static block
                    MethodVisitor mv = super.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                    mv = new StaticBlockMethodVisitor(mv);
                    mv.visitCode();
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                super.visitEnd();
            }
        };

        // feed the original class to the wrapped ClassVisitor
        cr.accept(cv, 0);

        // produce the modified class
        byte[] newClassData = cw.toByteArray();
        return newClassData;
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
