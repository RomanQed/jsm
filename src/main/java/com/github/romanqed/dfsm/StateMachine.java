package com.github.romanqed.dfsm;

/**
 * Interface describing a finite state machine.
 *
 * @param <T> "symbol" type of finite state machine
 */
public interface StateMachine<T> {

    /**
     * Performs a transition for the specified token.
     *
     * @param token analyzed "symbol"
     * @return machine state after transition
     */
    int step(T token);

    /**
     * Launches a finite state machine on a token chain.
     * Does not change the internal state of the machine.
     *
     * @param tokens chain of analyzed "symbols"
     * @return final machine state after token processing
     */
    int run(Iterable<T> tokens);

    /**
     * 
     */
    void reset();
}
