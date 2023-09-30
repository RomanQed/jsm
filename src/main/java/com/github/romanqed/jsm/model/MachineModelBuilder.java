package com.github.romanqed.jsm.model;

import java.util.*;

/**
 * A class representing a builder for a finite state machine model.
 *
 * @param <S> state type
 * @param <T> token type
 */
public final class MachineModelBuilder<S, T> {
    private final Class<S> stateType;
    private final Class<T> tokenType;
    private final Map<S, Map<S, Transition<S, T>>> transitions;
    private final Set<S> states;
    private S init;
    private S exit;

    public MachineModelBuilder(Class<S> stateType, Class<T> tokenType) {
        this.stateType = Objects.requireNonNull(stateType);
        this.tokenType = Objects.requireNonNull(tokenType);
        this.transitions = new HashMap<>();
        this.states = new HashSet<>();
    }

    /**
     * Creates a builder with the specified types.
     *
     * @param stateType {@link Class} instance, contains state type
     * @param tokenType {@link Class} instance, contains token type
     * @param <K>       state type
     * @param <V>       token type
     * @return instance of {@link MachineModelBuilder}
     */
    public static <K, V> MachineModelBuilder<K, V> create(Class<K> stateType, Class<V> tokenType) {
        return new MachineModelBuilder<>(stateType, tokenType);
    }

    private void reset() {
        init = null;
        exit = null;
        transitions.clear();
        states.clear();
    }

    private void checkState(S state) {
        if (state != null && state.getClass() != stateType) {
            throw new InvalidStateException("The class of the state object is not equal to the expected class", state);
        }
    }

    private void checkToken(T token) {
        if (token != null && token.getClass() != tokenType) {
            throw new IllegalArgumentException("The class of the token object is not equal to the expected class");
        }
    }

    /**
     * Sets the state that the finite state machine receives at the beginning of operation.
     *
     * @param state initial state
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> setInitState(S state) {
        checkState(state);
        if (Objects.equals(state, init)) {
            return this;
        }
        if (Objects.equals(state, exit)) {
            throw new InvalidStateException("The initial state should be different from the exit state", state);
        }
        this.transitions.remove(state);
        this.transitions.put(state, new HashMap<>());
        this.init = state;
        return this;
    }

    /**
     * Sets the default state that will be used by the machine if it is impossible to switch to another state.
     *
     * @param state default state
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> setExitState(S state) {
        checkState(state);
        if (Objects.equals(state, exit)) {
            return this;
        }
        if (Objects.equals(state, init)) {
            throw new InvalidStateException("The exit state should be different from the initial state", state);
        }
        this.transitions.values().forEach(value -> value.remove(state));
        this.exit = state;
        return this;
    }

    /**
     * Adds a new state to the finite state machine.
     *
     * @param state state key
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> addState(S state) {
        checkState(state);
        if (Objects.equals(this.init, state)) {
            throw new InvalidStateException("The intermediate state must be different from the input state", state);
        }
        if (Objects.equals(this.exit, state)) {
            throw new InvalidStateException("The intermediate state must be different from the output state", state);
        }
        if (!this.states.add(state)) {
            return this;
        }
        this.transitions.put(state, new HashMap<>());
        return this;
    }

    /**
     * Removes a state and its transitions from the finite state machine.
     *
     * @param state state key
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> removeState(S state) {
        checkState(state);
        if (!this.states.remove(state)) {
            return this;
        }
        this.transitions.remove(state);
        this.transitions.values().forEach(value -> value.remove(state));
        return this;
    }

    private void addTransition(S from, S to, T token, TransitionType type) {
        checkState(from);
        checkState(to);
        checkToken(token);
        var map = transitions.get(from);
        if (map == null) {
            throw new InvalidStateException("Required source state cannot have outgoing transitions", from);
        }
        if (!Objects.equals(exit, to) && !states.contains(to)) {
            throw new InvalidStateException("Required target state not found", to);
        }
        var transition = new Transition<>(to, token, type);
        map.put(to, transition);
    }

    /**
     * Adds a new conditional transition to the finite state machine.
     *
     * @param from  source state key
     * @param to    target state key
     * @param token token value
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> addTransition(S from, S to, T token) {
        addTransition(from, to, token, TransitionType.CONDITIONAL);
        return this;
    }

    /**
     * Adds a new unconditional transition to the finite state machine.
     *
     * @param from source state key
     * @param to   target state key
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> addTransition(S from, S to) {
        addTransition(from, to, null, TransitionType.UNCONDITIONAL);
        return this;
    }

    /**
     * Removes a transition from a finite state machine.
     *
     * @param from source state key
     * @param to   target state key
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> removeTransition(S from, S to) {
        checkState(from);
        checkState(to);
        var map = this.transitions.get(from);
        if (map == null) {
            return this;
        }
        map.remove(to);
        return this;
    }

    private Set<Transition<S, T>> collectTransitions(S state, Set<S> unreachable) {
        var ret = new HashSet<Transition<S, T>>();
        transitions.get(state).forEach((k, v) -> {
            unreachable.remove(k);
            ret.add(v);
        });
        return ret;
    }

    /**
     * Completes the build of the finite state machine model and returns the result.
     *
     * @return built finite state machine model
     */
    public MachineModel<S, T> build() {
        if (Objects.equals(init, exit)) {
            throw new IllegalStateException("Initial and exit state must be different");
        }
        var states = new HashSet<State<S, T>>();
        var unreachable = new HashSet<>(this.states);
        // Process init state
        var init = new State<>(this.init, collectTransitions(this.init, unreachable));
        // Process inner states
        for (var state : this.states) {
            states.add(new State<>(state, collectTransitions(state, unreachable)));
        }
        if (!unreachable.isEmpty()) {
            throw new IllegalStateException("Unreachable states found: " + unreachable);
        }
        // Process exit state
        var exit = new State<S, T>(this.exit);
        var ret = new MachineModel<>(tokenType, init, exit, states);
        this.reset();
        return ret;
    }
}
