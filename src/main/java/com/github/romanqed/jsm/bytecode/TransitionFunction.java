package com.github.romanqed.jsm.bytecode;

/**
 * An interface describing the transition function of a finite state machine.
 *
 * @param <T> token type
 */
public interface TransitionFunction<T> {

    /**
     * Selects the next state of the state machine based on the previous state and the analyzed token.
     *
     * @param state previous machine state
     * @param token analyzed token
     * @return next machine state
     */
    int transit(int state, T token);
}
