package com.github.romanqed.jsm;

/**
 * Interface describing a finite state machine.
 *
 * @param <S> state type
 * @param <T> token type
 */
public interface StateMachine<S, T> {

    /**
     * Runs the finite state machine on a token chain.
     * Does not change the internal state of the machine.
     *
     * @param tokens the chain of analyzed tokens
     * @return final machine state after token processing
     */
    S run(Iterable<T> tokens);

    /**
     * Runs the finite state machine on a token array.
     * Does not change the internal state of the machine.
     *
     * @param tokens the array contains analyzed tokens
     * @return final machine state after token processing
     */
    S run(T[] tokens);

    /**
     * Starts the finite state machine and calculates a unique stamp for
     * the sequence of states that the machine has passed through.
     * <br>
     * That is, for example, if the machine switched states [a, b, c] step by step,
     * then the stamp will contain the result of a some hash function: stamp := hash([a, b, c]).
     * If the machine has reached the exit state, the stamp will be -1.
     *
     * @param tokens the chain of analyzed tokens
     * @return the stamp of this run
     */
    long stamp(Iterable<T> tokens);

    /**
     * Starts the finite state machine and calculates a unique stamp for
     * the sequence of states that the machine has passed through.
     * <br>
     * That is, for example, if the machine switched states [a, b, c] step by step,
     * then the stamp will contain the result of a some hash function: stamp := hash([a, b, c]).
     * If the machine has reached the exit state, the stamp will be -1.
     *
     * @param tokens the array contains analyzed tokens
     * @return the stamp of this run
     */
    long stamp(T[] tokens);

    /**
     * Returns current machine state.
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
     * Resets the state of the state machine to its initial state.
     */
    void reset();
}
