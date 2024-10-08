package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jsm.StateMachine;

final class BytecodeMachine<S, T> implements StateMachine<S, T> {
    private final TransitionFunction<T> function;
    private final S[] from;
    private final int init;
    private final int exit;
    private int state;

    BytecodeMachine(TransitionFunction<T> function, S[] from, int init, int exit) {
        this.function = function;
        this.from = from;
        this.init = init;
        this.exit = exit;
        this.state = init;
    }

    @Override
    public S run(Iterable<T> tokens) {
        var state = this.init;
        for (var token : tokens) {
            state = function.transit(state, token);
            if (state == exit) {
                return from[exit];
            }
        }
        return from[state];
    }

    @Override
    public S run(T[] tokens) {
        var state = this.init;
        for (var token : tokens) {
            state = function.transit(state, token);
            if (state == exit) {
                return from[exit];
            }
        }
        return from[state];
    }

    @Override
    @SuppressWarnings("Duplicates")
    public long stamp(Iterable<T> tokens) {
        var state = this.init;
        var ret = 1;
        for (var token : tokens) {
            state = function.transit(state, token);
            if (state == exit) {
                return -1;
            }
            ret = 31 * ret + state;
        }
        return ret;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public long stamp(T[] tokens) {
        var state = this.init;
        var ret = 1;
        for (var token : tokens) {
            state = function.transit(state, token);
            if (state == exit) {
                return -1;
            }
            ret = 31 * ret + state;
        }
        return ret;
    }

    @Override
    public S getState() {
        return from[this.state];
    }

    @Override
    public S step(T token) {
        this.state = function.transit(this.state, token);
        return from[this.state];
    }

    @Override
    public void reset() {
        this.state = init;
    }
}
