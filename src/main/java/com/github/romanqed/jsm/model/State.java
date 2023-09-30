package com.github.romanqed.jsm.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A class describing the state of a finite machine.
 *
 * @param <S> state type
 * @param <T> token type
 */
public final class State<S, T> {
    private final S value;
    private final Set<Transition<S, T>> transitions;

    State(S value, Set<Transition<S, T>> transitions) {
        this.value = value;
        this.transitions = Collections.unmodifiableSet(transitions);
    }

    State(S value) {
        this.value = value;
        this.transitions = Set.of();
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
     * Returns the set of transitions possible from this state.
     *
     * @return the set of transitions possible from this state
     */
    public Set<Transition<S, T>> getTransitions() {
        return transitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var state = (State<?, ?>) o;
        return Objects.equals(value, state.value);
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
                '}';
    }
}
