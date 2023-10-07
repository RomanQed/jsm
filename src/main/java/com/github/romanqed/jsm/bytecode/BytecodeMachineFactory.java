package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jeflect.DefineClassLoader;
import com.github.romanqed.jeflect.DefineLoader;
import com.github.romanqed.jeflect.DefineObjectFactory;
import com.github.romanqed.jeflect.ObjectFactory;
import com.github.romanqed.jsm.StateMachine;
import com.github.romanqed.jsm.StateMachineFactory;
import com.github.romanqed.jsm.model.MachineModel;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a finite state machine factory using jit compilation of the transition function.
 */
public final class BytecodeMachineFactory implements StateMachineFactory {
    private static final String FUNCTION_NAME = "TransitionFunction";
    private final ObjectFactory<TransitionFunction<?>> factory;
    private final Map<String, Map<Integer, ?>> translations;

    public BytecodeMachineFactory(ObjectFactory<TransitionFunction<?>> factory) {
        this.factory = Objects.requireNonNull(factory);
        this.translations = new ConcurrentHashMap<>();
    }

    public BytecodeMachineFactory(DefineLoader loader) {
        this(new DefineObjectFactory<>(loader));
    }

    public BytecodeMachineFactory() {
        this(new DefineObjectFactory<>(new DefineClassLoader()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> StateMachine<S, T> create(MachineModel<S, T> model) {
        var spec = model.format();
        var name = FUNCTION_NAME + spec.hashCode();
        var function = (TransitionFunction<T>) factory.create(name, () -> {
            var translation = Translation.make(model);
            translations.computeIfAbsent(spec, k -> translation.getFrom());
            return Util.generateTransitionFunction(name, model, translation);
        });
        var translation = (Map<Integer, S>) translations.get(spec);
        if (translation == null) {
            throw new IllegalStateException("Translation for spec " + spec + " not found");
        }
        var init = translation.entrySet()
                .stream()
                .filter(entry -> Objects.equals(model.getInit().getValue(), entry.getValue()))
                .findFirst()
                .orElseThrow()
                .getKey();
        return new BytecodeMachine<>(function, translation, init);
    }
}
