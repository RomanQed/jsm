package com.github.romanqed.jsm.model;

import java.util.Collections;
import java.util.Set;

/**
 * @param <S> state type
 * @param <T> token type
 */
public final class MachineModel<S, T> {
    private final Class<T> tokenType;
    private final State<S, T> init;
    private final State<S, T> exit;
    private final Set<State<S, T>> states;

    MachineModel(Class<T> tokenType, State<S, T> init, State<S, T> exit, Set<State<S, T>> states) {
        this.tokenType = tokenType;
        this.init = init;
        this.exit = exit;
        this.states = Collections.unmodifiableSet(states);
    }

    /**
     * Returns token type as instance of {@link Class}.
     *
     * @return token type
     */
    public Class<T> getTokenType() {
        return tokenType;
    }

    /**
     * Returns the initial state of the finite state machine.
     *
     * @return the initial state of the finite state machine
     */
    public State<S, T> getInit() {
        return init;
    }

    /**
     * Returns the exit state of the finite state machine.
     *
     * @return the exit state of the finite state machine
     */
    public State<S, T> getExit() {
        return exit;
    }

    /**
     * Returns a set of intermediate states of a finite state machine.
     *
     * @return a set of intermediate states of a finite state machine
     */
    public Set<State<S, T>> getStates() {
        return states;
    }

    @Override
    public String toString() {
        return "MachineModel{" +
                "init=" + init +
                ", exit=" + exit +
                ", states=" + states +
                '}';
    }
}
