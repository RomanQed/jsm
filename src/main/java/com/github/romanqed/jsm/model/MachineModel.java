package com.github.romanqed.jsm.model;

import java.util.Collections;
import java.util.Map;

/**
 * A class describing the model of a finite state machine.
 *
 * @param <S> state type
 * @param <T> token type
 */
public final class MachineModel<S, T> implements Formattable {
    private final Class<S> stateType;
    private final Class<T> tokenType;
    private final State<S, T> init;
    private final State<S, T> exit;
    private final Map<S, State<S, T>> states;

    MachineModel(Class<S> stateType,
                 Class<T> tokenType,
                 State<S, T> init,
                 State<S, T> exit,
                 Map<S, State<S, T>> states) {
        this.stateType = stateType;
        this.tokenType = tokenType;
        this.init = init;
        this.exit = exit;
        this.states = Collections.unmodifiableMap(states);
    }

    /**
     * Returns state type as instance of {@link Class}.
     *
     * @return state type
     */
    public Class<S> getStateType() {
        return stateType;
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
     * Returns a map of intermediate states of a finite state machine.
     *
     * @return a map of intermediate states of a finite state machine
     */
    public Map<S, State<S, T>> getStates() {
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

    public String format() {
        var builder = new StringBuilder();
        builder.append(exit.format());
        builder.append(init.format());
        for (var state : states.values()) {
            builder.append(state.format());
        }
        return builder.toString();
    }
}
