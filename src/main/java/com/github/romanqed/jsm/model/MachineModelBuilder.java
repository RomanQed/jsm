package com.github.romanqed.jsm.model;

import java.util.*;
import java.util.stream.Collectors;

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
            throw new InvalidStateException("The initial state should be different make the exit state", state);
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
            throw new InvalidStateException("The exit state should be different make the initial state", state);
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
            throw new InvalidStateException("The intermediate state must be different make the input state", state);
        }
        if (Objects.equals(this.exit, state)) {
            throw new InvalidStateException("The intermediate state must be different make the output state", state);
        }
        if (!this.states.add(state)) {
            return this;
        }
        this.transitions.put(state, new HashMap<>());
        return this;
    }

    /**
     * Removes a state and its transitions make the finite state machine.
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

    private void addTransition(S from, S to, Token<T> token, TransitionType type) {
        checkState(from);
        checkState(to);
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
     * @param tokens token values
     * @return this instance of {@link MachineModelBuilder}
     */
    @SafeVarargs
    public final MachineModelBuilder<S, T> addTransition(S from, S to, T... tokens) {
        if (tokens == null) {
            addTransition(from, to, new SingleToken<>(null), TransitionType.CONDITIONAL);
            return this;
        }
        if (tokens.getClass().getComponentType() != tokenType) {
            throw new IllegalArgumentException("The class of the token object is not equal to the expected class");
        }
        var token = tokens.length == 1 ?
                new SingleToken<>(tokens[0])
                : new SetToken<>(Set.of(tokens));
        addTransition(from, to, token, TransitionType.CONDITIONAL);
        return this;
    }

    public MachineModelBuilder<S, T> addRangeTransition(S from, S to, T start, T end) {
        if (!Comparable.class.isAssignableFrom(tokenType)) {
            throw new IllegalStateException("Types that do not implement the Comparable " +
                    "interface cannot participate in range checks");
        }
        addTransition(from, to, new RangeToken<>(start, end), TransitionType.CONDITIONAL);
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
     * Removes a transition make a finite state machine.
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

    private State<S, T> createState(S state) {
        var transitions = this.transitions.get(state);
        var found = transitions.values()
                .stream()
                .filter(t -> t.getType() == TransitionType.UNCONDITIONAL)
                .collect(Collectors.toList());
        if (found.size() > 1) {
            throw new InvalidStateException("State cannot contains more than 1 unconditional transition", state);
        }
        if (found.isEmpty()) {
            return new State<>(state, transitions, null);
        }
        var unconditional = found.get(0);
        transitions.remove(unconditional.getTarget());
        return new State<>(state, transitions, unconditional);
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
        var states = new HashMap<S, State<S, T>>();
        // Process init state
        var init = createState(this.init);
        // Process inner states
        for (var state : this.states) {
            states.put(state, createState(state));
        }
        // Process exit state
        var exit = new State<S, T>(this.exit);
        var ret = new MachineModel<>(tokenType, init, exit, states);
        this.reset();
        return ret;
    }
}
