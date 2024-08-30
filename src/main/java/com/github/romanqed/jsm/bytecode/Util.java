package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jfunc.Exceptions;
import com.github.romanqed.jsm.model.MachineModel;
import com.github.romanqed.jsm.model.SingleToken;
import com.github.romanqed.jsm.model.State;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class Util {
    static final Type OBJECT = Type.getType(Object.class);
    static final String OBJECT_NAME = OBJECT.getInternalName();
    private static final Class<?> INTERFACE = TransitionFunction.class;
    private static final Method TRANSIT = Exceptions.suppress(
            () -> INTERFACE.getDeclaredMethod("transit", int.class, Object.class)
    );
    private static final Method HASH_CODE = Exceptions.suppress(
            () -> Object.class.getDeclaredMethod("hashCode")
    );
    private static final String INIT = "<init>";
    private static final String EMPTY_DESCRIPTOR = "()V";

    private static SwitchMap<State<?, ?>> makeTableMap(MachineModel<?, ?> model, Map<Integer, ?> translation) {
        var init = model.getInit();
        var states = model.getStates();
        var mapper = (Function<Integer, State<?, ?>>) key -> {
            if (key == 1) {
                return init;
            }
            var value = translation.get(key);
            return states.get(value);
        };
        var ret = new TableSwitchMap<>(mapper, 1, states.size() + 1);
        ret.put(model.getInit());
        states.values().forEach(ret::put);
        return ret;
    }

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

    private static void processExit(State<?, ?> state, MethodVisitor visitor, int exit, Map<?, Integer> translation) {
        var unconditional = state.getUnconditional();
        if (unconditional == null) {
            pushInt(visitor, exit);
        } else {
            pushInt(visitor, translation.get(unconditional.getTarget()));
        }
        visitor.visitInsn(Opcodes.IRETURN);
    }

    private static void processState(State<?, ?> state,
                                     MethodVisitor visitor,
                                     int buffer,
                                     int exit,
                                     Map<?, Integer> translation) {
        var transitions = state.getTransitions().values();
        // Handle empty transitions
        if (transitions.isEmpty()) {
            processExit(state, visitor, exit, translation);
            return;
        }
        visitor.visitVarInsn(Opcodes.ILOAD, buffer);
        // Handle 1 transitions
        if (transitions.size() == 1) {
            var transition = transitions.iterator().next();
            var token = transition.getToken();
            if (token instanceof SingleToken) {
                var single = (SingleToken<?>) token;
                var out = new Label();
                pushInt(visitor, single.getValue().hashCode());
                visitor.visitJumpInsn(Opcodes.IF_ICMPNE, out);
                pushInt(visitor, translation.get(transition.getTarget()));
                visitor.visitInsn(Opcodes.IRETURN);
                visitor.visitLabel(out);
                processExit(state, visitor, exit, translation);
                return;
            }
        }
        // Handle other cases
        var map = new HashMap<Integer, Integer>();
        for (var transition : state.getTransitions().values()) {
            var target = translation.get(transition.getTarget());
            var handler = new MapVisitor(map, target);
            transition.getToken().accept(handler);
        }
        var table = new LookupSwitchMap<>(Function.identity());
        map.keySet().forEach(table::put);
        table.visitSwitch(visitor, v -> processExit(state, visitor, exit, translation), (v, value) -> {
            pushInt(v, map.get(value));
            v.visitInsn(Opcodes.IRETURN);
        });
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

    static byte[] generateTransitionFunction(String name, MachineModel<?, ?> model, Translation translation) {
        // Init class writer
        var writer = new LocalVariablesWriter(ClassWriter.COMPUTE_FRAMES);
        // Declare class header
        writer.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(INTERFACE)});
        // Define empty constructor
        createEmptyConstructor(writer);
        // Define transit method
        var visitor = writer.visitMethodWithLocals(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                TRANSIT.getName(),
                Type.getMethodDescriptor(TRANSIT),
                null,
                null);
        visitor.visitCode();
        // {
        // Define buffer: int buffer;
        var buffer = visitor.newLocal(Type.INT_TYPE);
        // Calculate hash code: buffer = arg@2 == null ? 0 : arg@2.hashCode();
        var invoke = new Label();
        var store = new Label();
        visitor.visitVarInsn(Opcodes.ALOAD, 2);
        visitor.visitInsn(Opcodes.ACONST_NULL);
        visitor.visitJumpInsn(Opcodes.IF_ACMPNE, invoke);
        // If arg@2 == null
        visitor.visitInsn(Opcodes.ICONST_0);
        visitor.visitJumpInsn(Opcodes.GOTO, store);
        // If arg@2 != null
        visitor.visitLabel(invoke);
        visitor.visitVarInsn(Opcodes.ALOAD, 2);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(HASH_CODE.getDeclaringClass()),
                HASH_CODE.getName(),
                Type.getMethodDescriptor(HASH_CODE),
                false);
        // Store hash to buffer
        visitor.visitLabel(store);
        visitor.visitVarInsn(Opcodes.ISTORE, buffer);
        // Load state from parameter
        visitor.visitVarInsn(Opcodes.ILOAD, 1);
        // Build table-switch descriptor
        var map = makeTableMap(model, translation.getFrom());
        // Prepare data
        var to = translation.getTo();
        var def = to.get(model.getExit().getValue());
        map.visitSwitch(visitor, v -> {
            pushInt(v, def);
            visitor.visitInsn(Opcodes.IRETURN);
        }, (v, state) -> processState(state, v, buffer, def, to));
        // }
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}
