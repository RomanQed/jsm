package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jsm.StateMachine;

import java.util.Map;

final class BytecodeMachine<S, T> implements StateMachine<S, T> {
    private final TransitionFunction<T> function;
    private final Map<Integer, S> translations;
    private final int init;
    private final int exit;
    private final Object lock;
    private int state;

    BytecodeMachine(TransitionFunction<T> function, Map<Integer, S> translations, int init, int exit) {
        this.function = function;
        this.translations = translations;
        this.init = init;
        this.exit = exit;
        this.state = init;
        this.lock = new Object();
    }

    @Override
    public S step(T token) {
        synchronized (lock) {
            this.state = function.transit(this.state, token);
            return translations.get(this.state);
        }
    }

    @Override
    public S run(Iterable<T> tokens) {
        var state = this.init;
        for (var token : tokens) {
            state = function.transit(state, token);
            if (state == exit) {
                return translations.get(exit);
            }
        }
        return translations.get(state);
    }

    @Override
    public S run(T[] tokens) {
        var state = this.init;
        for (var token : tokens) {
            state = function.transit(state, token);
            if (state == exit) {
                return translations.get(exit);
            }
        }
        return translations.get(state);
    }

    @Override
    public void reset() {
        synchronized (lock) {
            this.state = init;
        }
    }
}
