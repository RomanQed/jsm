package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jeflect.AsmUtil;
import com.github.romanqed.jfunc.Exceptions;
import com.github.romanqed.jsm.model.MachineModel;
import com.github.romanqed.jsm.model.SingleToken;
import com.github.romanqed.jsm.model.State;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class Util {
    private static final Class<?> INTERFACE = TransitionFunction.class;
    private static final Method TRANSIT = Exceptions.suppress(
            () -> INTERFACE.getDeclaredMethod("transit", int.class, Object.class)
    );
    private static final Method HASH_CODE = Exceptions.suppress(
            () -> Object.class.getDeclaredMethod("hashCode")
    );

    private static LocalVariablesSorter visitMethod(ClassWriter writer,
                                                    int access,
                                                    String name,
                                                    String descriptor) {
        var visitor = writer.visitMethod(access, name, descriptor, null, null);
        return new LocalVariablesSorter(access, descriptor, visitor);
    }

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

    private static void processExit(State<?, ?> state, MethodVisitor visitor, int exit, Map<?, Integer> translation) {
        var unconditional = state.getUnconditional();
        if (unconditional == null) {
            AsmUtil.pushInt(visitor, exit);
        } else {
            AsmUtil.pushInt(visitor, translation.get(unconditional.getTarget()));
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
                AsmUtil.pushInt(visitor, single.getValue().hashCode());
                visitor.visitJumpInsn(Opcodes.IF_ICMPNE, out);
                AsmUtil.pushInt(visitor, translation.get(transition.getTarget()));
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
            AsmUtil.pushInt(v, map.get(value));
            v.visitInsn(Opcodes.IRETURN);
        });
    }

    static byte[] generateTransitionFunction(String name, MachineModel<?, ?> model, Translation translation) {
        // Init class writer
        var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // Declare class header
        writer.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(INTERFACE)});
        // Define empty constructor
        AsmUtil.createEmptyConstructor(writer);
        // Define transit method
        var visitor = visitMethod(writer,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                TRANSIT.getName(),
                Type.getMethodDescriptor(TRANSIT));
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
            AsmUtil.pushInt(v, def);
            visitor.visitInsn(Opcodes.IRETURN);
        }, (v, state) -> processState(state, v, buffer, def, to));
        // }
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}
