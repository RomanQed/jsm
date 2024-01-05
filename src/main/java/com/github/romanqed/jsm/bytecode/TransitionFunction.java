package com.github.romanqed.jsm.bytecode;

/**
 * An interface describing a function used inside the bytecode state machine.
 *
 * @param <T> token type
 */
public interface TransitionFunction<T> {
    int transit(int state, T token);
}
