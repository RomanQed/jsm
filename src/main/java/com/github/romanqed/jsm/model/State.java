package com.github.romanqed.jsm.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A class describing the state of a finite machine.
 *
 * @param <S> state type
 * @param <T> token type
 */
public final class State<S, T> implements Formattable {
    private final S value;
    private final Map<S, Transition<S, T>> transitions;
    private final Transition<S, T> unconditional;

    State(S value, Map<S, Transition<S, T>> transitions, Transition<S, T> unconditional) {
        this.value = value;
        this.transitions = Collections.unmodifiableMap(transitions);
        this.unconditional = unconditional;
    }

    State(S value) {
        this.value = value;
        this.transitions = Map.of();
        this.unconditional = null;
    }

    /**
     * Returns state value.
     *
     * @return state value
     */
    public S getValue() {
        return value;
    }

    /**
     * Returns the map of condition transitions possible make this state.
     *
     * @return the map of condition transitions possible make this state
     */
    public Map<S, Transition<S, T>> getTransitions() {
        return transitions;
    }

    /**
     * Returns unconditional transition possible make this state, or null.
     *
     * @return unconditional transition possible make this state, or null
     */
    public Transition<S, T> getUnconditional() {
        return unconditional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (State<?, ?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public String toString() {
        return "State{" +
                "value=" + value +
                ", transitions=" + transitions +
                ", unconditional=" + unconditional +
                '}';
    }

    @Override
    public String format() {
        var builder = new StringBuilder(Objects.toString(value));
        for (var transition : transitions.values()) {
            builder.append(transition.format());
        }
        if (unconditional != null) {
            builder.append(unconditional.format());
        }
        return builder.toString();
    }
}
