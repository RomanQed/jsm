package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jeflect.AsmUtil;
import com.github.romanqed.jfunc.Exceptions;
import com.github.romanqed.jsm.model.MachineModel;
import com.github.romanqed.jsm.model.State;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

final class Util {
    private static final Class<?> LAMBDA_CLASS = TransitionFunction.class;
    private static final Method TRANSIT = Exceptions.suppress(
            () -> LAMBDA_CLASS.getDeclaredMethod("transit", int.class, Object.class)
    );
    private static final String STRING_EQUALS = "(Ljava/lang/Object;)Z";
    private static final String FLOAT_COMPARE = "(FF)I";
    private static final String DOUBLE_COMPARE = "(DD)I";
    private static final Map<Class<?>, Integer> LOAD_OPCODES = Map.of(
            Boolean.class, Opcodes.ILOAD,
            Character.class, Opcodes.ILOAD,
            Byte.class, Opcodes.ILOAD,
            Short.class, Opcodes.ILOAD,
            Integer.class, Opcodes.ILOAD,
            Long.class, Opcodes.LLOAD,
            Float.class, Opcodes.FLOAD,
            Double.class, Opcodes.DLOAD
    );

    private static final Map<Class<?>, Integer> STORE_OPCODES = Map.of(
            Boolean.class, Opcodes.ISTORE,
            Character.class, Opcodes.ISTORE,
            Byte.class, Opcodes.ISTORE,
            Short.class, Opcodes.ISTORE,
            Integer.class, Opcodes.ISTORE,
            Long.class, Opcodes.LSTORE,
            Float.class, Opcodes.FSTORE,
            Double.class, Opcodes.DSTORE
    );

    private static final Map<Class<?>, Class<?>> PRIMITIVES = Map.of(
            Boolean.class, boolean.class,
            Character.class, char.class,
            Byte.class, byte.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class
    );

    static void compare(MethodVisitor visitor, Label out, Class<?> type) {
        if (type == Float.class) {
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(Float.class),
                    "compare",
                    FLOAT_COMPARE,
                    false);
            visitor.visitJumpInsn(Opcodes.IFNE, out);
            return;
        }
        if (type == Double.class) {
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(Double.class),
                    "compare",
                    DOUBLE_COMPARE,
                    false);
            visitor.visitJumpInsn(Opcodes.IFNE, out);
            return;
        }
        if (type == Long.class) {
            visitor.visitInsn(Opcodes.LCMP);
            visitor.visitJumpInsn(Opcodes.IFNE, out);
            return;
        }
        if (type == String.class) {
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "equals",
                    STRING_EQUALS,
                    false);
            visitor.visitJumpInsn(Opcodes.IFEQ, out);
            return;
        }
        visitor.visitJumpInsn(Opcodes.IF_ICMPNE, out);
    }

    static void processToken(MethodVisitor visitor, Label out, int index, Object token) {
        AsmUtil.pushConstant(visitor, token);
        var type = token.getClass();
        visitor.visitVarInsn(LOAD_OPCODES.getOrDefault(type, Opcodes.ALOAD), index);
        compare(visitor, out, type);
    }

    static void processState(MethodVisitor visitor,
                             Label start,
                             int index,
                             State<?, ?> state,
                             int exit,
                             Map<?, Integer> table) {
        visitor.visitLabel(start);
        for (var transition : state.getTransitions().values()) {
            var out = new Label();
            processToken(visitor, out, index, transition.getToken());
            AsmUtil.pushInt(visitor, table.get(transition.getTarget()));
            visitor.visitInsn(Opcodes.IRETURN);
            visitor.visitLabel(out);
        }
        var unconditional = state.getUnconditional();
        if (unconditional == null) {
            AsmUtil.pushInt(visitor, exit);
        } else {
            AsmUtil.pushInt(visitor, table.get(unconditional.getTarget()));
        }
        visitor.visitInsn(Opcodes.IRETURN);
    }

    static byte[] generateTransitionFunction(String name, MachineModel<?, ?> model, Translation translation) {
        // Init writer
        var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // Declare class header
        writer.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                AsmUtil.OBJECT.getInternalName(),
                new String[]{Type.getType(LAMBDA_CLASS).getInternalName()});
        // Create empty constructor
        AsmUtil.createEmptyConstructor(writer);
        // Prepare data
        var states = model.getStates();
        var exit = new Label();
        var to = translation.getTo();
        var exitCode = to.get(model.getExit().getValue());
        var labels = new Label[states.size() + 1];
        Arrays.setAll(labels, e -> new Label());
        var type = model.getTokenType();
        // Define transit method
        var visitor = new LocalVariablesSorter(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                Type.getMethodDescriptor(TRANSIT),
                writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                        TRANSIT.getName(),
                        Type.getMethodDescriptor(TRANSIT),
                        null,
                        null));
        visitor.visitCode();
        // {
        // If need unpack primitive, declare local buffer
        int index = 2;
        if (type != String.class) {
            var primitive = Type.getType(PRIMITIVES.get(type));
            index = visitor.newLocal(primitive);
            visitor.visitVarInsn(Opcodes.ALOAD, 2);
            AsmUtil.castReference(visitor, primitive);
            visitor.visitVarInsn(STORE_OPCODES.get(type), index);
        }
        // Load machine state
        visitor.visitVarInsn(Opcodes.ILOAD, 1);
        // Switch-case declaration
        visitor.visitTableSwitchInsn(1, labels.length, exit, labels);
        // Process init
        processState(visitor, labels[0], index, model.getInit(), exitCode, to);
        // Process intermediate states
        for (var i = 1; i < labels.length; ++i) {
            var key = translation.getFrom().get(i + 1);
            processState(visitor, labels[i], index, states.get(key), exitCode, to);
        }
        // Process exit
        visitor.visitLabel(exit);
        AsmUtil.pushInt(visitor, exitCode);
        visitor.visitInsn(Opcodes.IRETURN);
        // }
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}
