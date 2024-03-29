package com.github.romanqed.jsm;

/**
 * Interface describing a finite state machine.
 *
 * @param <S> state type
 * @param <T> token type
 */
public interface StateMachine<S, T> {

    /**
     * Launches a finite state machine on a token chain.
     * Does not change the internal state of the machine.
     *
     * @param tokens chain of analyzed tokens
     * @return final machine state after token processing
     */
    S run(Iterable<T> tokens);

    /**
     * Launches a finite state machine on a token array.
     * Does not change the internal state of the machine.
     *
     * @param tokens array contains analyzed tokens
     * @return final machine state after token processing
     */
    S run(T[] tokens);

    /**
     * Returns current machine state.
     * Synchronized with other step-by-step methods.
     *
     * @return current machine state
     */
    S getState();

    /**
     * Performs a transition for the specified token.
     *
     * @param token analyzed token
     * @return machine state after transition
     */
    S step(T token);

    /**
     * Resets the state of the state machine to its init state.
     */
    void reset();
}
