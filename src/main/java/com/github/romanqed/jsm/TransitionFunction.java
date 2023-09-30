package com.github.romanqed.jsm;

/**
 * An interface describing the transition function of a finite state machine.
 *
 * @param <T> "symbol" type of finite state machine
 */
public interface TransitionFunction<T> {
    /**
     * Selects the next state of the state machine based on the previous state and the analyzed token.
     *
     * @param state previous machine state
     * @param token analyzed "symbol"
     * @return next machine state
     */
    int transit(int state, T token);
}
