package com.github.romanqed.jsm.asm;

import com.github.romanqed.jsm.model.State;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

final class Util {
    private static final String OBJECT_NAME = "java/lang/Object";
    private static final String INIT = "<init>";
    private static final String EMPTY_DESCRIPTOR = "()V";
    private static final Set<Class<?>> TYPES = Set.of(
            // Boolean
            Boolean.class,
            // Char
            Character.class,
            // String
            String.class,
            // Int-types
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            // Float-types
            Float.class,
            Double.class
    );

    static void pushInt(MethodVisitor visitor, int value) {
        if (value >= -1 && value <= 5) {
            visitor.visitInsn(Opcodes.ICONST_M1 + value + 1);
            return;
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            visitor.visitIntInsn(Opcodes.BIPUSH, value);
            return;
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            visitor.visitIntInsn(Opcodes.SIPUSH, value);
            return;
        }
        visitor.visitLdcInsn(value);
    }

    static void processExit(State<?, ?> state, MethodVisitor visitor, int exit, Map<?, Integer> translation) {
        var unconditional = state.getUnconditional();
        if (unconditional == null) {
            pushInt(visitor, exit);
        } else {
            pushInt(visitor, translation.get(unconditional.getTarget()));
        }
        visitor.visitInsn(Opcodes.IRETURN);
    }

    static void createEmptyConstructor(ClassWriter writer) {
        var init = writer.visitMethod(Opcodes.ACC_PUBLIC,
                INIT,
                EMPTY_DESCRIPTOR,
                null,
                null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_NAME, INIT, EMPTY_DESCRIPTOR, false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();
    }

    static void checkType(Class<?> type) {
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("Bytecode machine factory supports only primitive and string tokens");
        }
    }

    static void loadLong(MethodVisitor visitor, int index) {
        visitor.visitVarInsn(Opcodes.ALOAD, index);
        var owner = Type.getInternalName(Long.class);
        visitor.visitTypeInsn(Opcodes.CHECKCAST, owner);
        visitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                owner,
                "longValue",
                "()J",
                false
        );
    }

    static void loadDouble(MethodVisitor visitor, int index) {
        visitor.visitVarInsn(Opcodes.ALOAD, index);
        var owner = Type.getInternalName(Double.class);
        visitor.visitTypeInsn(Opcodes.CHECKCAST, owner);
        visitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                owner,
                "doubleValue",
                "()D",
                false
        );
    }

    static Consumer<MethodVisitor> getLoader(Class<?> type, int index) {
        if (type == Long.class) {
            return v -> loadLong(v, index);
        }
        if (type == Double.class) {
            return v -> loadDouble(v, index);
        }
        if (type == String.class) {
            return v -> v.visitVarInsn(Opcodes.ALOAD, index);
        }
        return null;
    }
}
