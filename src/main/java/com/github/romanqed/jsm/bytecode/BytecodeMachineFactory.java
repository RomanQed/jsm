package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.asm.sorter.LocalVariablesWriter;
import com.github.romanqed.jeflect.loader.DefineClassLoader;
import com.github.romanqed.jeflect.loader.DefineLoader;
import com.github.romanqed.jeflect.loader.DefineObjectFactory;
import com.github.romanqed.jeflect.loader.ObjectFactory;
import com.github.romanqed.jfunc.Exceptions;
import com.github.romanqed.jsm.StateMachine;
import com.github.romanqed.jsm.StateMachineFactory;
import com.github.romanqed.jsm.model.MachineModel;
import com.github.romanqed.jsm.model.SingleToken;
import com.github.romanqed.jsm.model.State;
import com.github.romanqed.switchgen.SwitchMaps;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implementation of a finite state machine factory using jit compilation of the transition function.
 */
public final class BytecodeMachineFactory implements StateMachineFactory {
    private static final int DEFAULT_MAX_DELTA = 10;
    private static final String FUNCTION_NAME = "TransitionFunction";
    private static final Class<?> INTERFACE = TransitionFunction.class;
    private static final Method TRANSIT = Exceptions.suppress(
            () -> INTERFACE.getDeclaredMethod("transit", int.class, Object.class)
    );
    private static final Method HASH_CODE = Exceptions.suppress(
            () -> Object.class.getDeclaredMethod("hashCode")
    );
    private static final int STATE_INDEX = 1;
    private static final int TOKEN_INDEX = 2;
    private final ObjectFactory<TransitionFunction<?>> factory;
    private final Map<String, Object[]> translations;
    private final int maxDelta;

    public BytecodeMachineFactory(ObjectFactory<TransitionFunction<?>> factory, int maxDelta) {
        this.factory = Objects.requireNonNull(factory);
        this.translations = new ConcurrentHashMap<>();
        this.maxDelta = maxDelta;
    }

    public BytecodeMachineFactory(DefineLoader loader, int maxDelta) {
        this(new DefineObjectFactory<>(loader), maxDelta);
    }

    public BytecodeMachineFactory(int maxDelta) {
        this(new DefineClassLoader(), maxDelta);
    }

    public BytecodeMachineFactory() {
        this(DEFAULT_MAX_DELTA);
    }

    private void processState(State<?, ?> state,
                              MethodVisitor visitor,
                              Consumer<MethodVisitor> loader,
                              int buffer,
                              int exit,
                              Map<?, Integer> translation) {
        var transitions = state.getTransitions().values();
        // Handle empty transitions
        if (transitions.isEmpty()) {
            Util.processExit(state, visitor, exit, translation);
            return;
        }
        visitor.visitVarInsn(Opcodes.ILOAD, buffer);
        // Handle 1 transition (if it is single token)
        if (transitions.size() == 1) {
            var transition = transitions.iterator().next();
            var token = transition.getToken();
            if (token instanceof SingleToken) {
                var single = (SingleToken<?>) token;
                var out = new Label();
                Util.pushInt(visitor, single.getValue().hashCode());
                visitor.visitJumpInsn(Opcodes.IF_ICMPNE, out);
                Util.pushInt(visitor, translation.get(transition.getTarget()));
                visitor.visitInsn(Opcodes.IRETURN);
                visitor.visitLabel(out);
                Util.processExit(state, visitor, exit, translation);
                return;
            }
        }
        // Handle other cases
        var map = new HashMap<Object, Integer>();
        for (var transition : state.getTransitions().values()) {
            var target = translation.get(transition.getTarget());
            var handler = new MapVisitor(map, target);
            transition.getToken().accept(handler);
        }
        var switchMap = SwitchMaps.create(map.keySet(), maxDelta);
        switchMap.visit(
                visitor,
                loader,
                v -> Util.processExit(state, v, exit, translation),
                (v, value) -> {
                    Util.pushInt(v, map.get(value));
                    v.visitInsn(Opcodes.IRETURN);
                }
        );
    }

    private byte[] generateTransitionFunction(String name, MachineModel<?, ?> model, Translation translation) {
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
        Util.createEmptyConstructor(writer);
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
        visitor.visitVarInsn(Opcodes.ALOAD, TOKEN_INDEX);
        visitor.visitInsn(Opcodes.ACONST_NULL);
        visitor.visitJumpInsn(Opcodes.IF_ACMPNE, invoke);
        // If arg@2 == null
        visitor.visitInsn(Opcodes.ICONST_0);
        visitor.visitJumpInsn(Opcodes.GOTO, store);
        // If arg@2 != null
        visitor.visitLabel(invoke);
        visitor.visitVarInsn(Opcodes.ALOAD, TOKEN_INDEX);
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(HASH_CODE.getDeclaringClass()),
                HASH_CODE.getName(),
                Type.getMethodDescriptor(HASH_CODE),
                false);
        // Store hash to buffer
        visitor.visitLabel(store);
        visitor.visitVarInsn(Opcodes.ISTORE, buffer);
        // Load state from parameter
        visitor.visitVarInsn(Opcodes.ILOAD, STATE_INDEX);
        // Build table-switch map
        var map = SwitchMaps.createTable(1, translation.size - 1);
        // Prepare data
        var to = translation.to;
        var exit = to.get(model.getExit().getValue());
        var states = model.getStates();
        var init = model.getInit();
        var loader = Util.getLoader(model.getTokenType(), TOKEN_INDEX);
        map.visit(
                visitor,
                null,
                v -> {
                    Util.pushInt(v, exit);
                    visitor.visitInsn(Opcodes.IRETURN);
                },
                (v, state) -> {
                    var resolved = state == 1 ? init : states.get(translation.from[state]);
                    processState(resolved, v, loader, buffer, exit, to);
                }
        );
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> StateMachine<S, T> create(MachineModel<S, T> model) {
        Util.checkType(model.getTokenType());
        var spec = model.format();
        var name = FUNCTION_NAME + spec.hashCode();
        var function = (TransitionFunction<T>) factory.create(name, () -> {
            var translation = Translation.of(model);
            translations.computeIfAbsent(spec, k -> translation.from);
            return generateTransitionFunction(name, model, translation);
        });
        var table = (S[]) translations.get(spec);
        if (table == null) {
            throw new IllegalStateException("Translation for spec " + spec + " not found");
        }
        return new BytecodeMachine<>(function, table, 1, 0);
    }
}
