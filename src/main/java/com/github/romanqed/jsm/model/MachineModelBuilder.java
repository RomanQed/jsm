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
    private final Map<S, S> unconditionals;
    private final Set<S> states;
    private S init;
    private S exit;

    public MachineModelBuilder(Class<S> stateType, Class<T> tokenType) {
        this.stateType = Objects.requireNonNull(stateType);
        this.tokenType = Objects.requireNonNull(tokenType);
        this.transitions = new HashMap<>();
        this.unconditionals = new HashMap<>();
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

    @SuppressWarnings("unchecked")
    private static void checkRange(Object left, Object right) {
        var comparable = (Comparable<Object>) left;
        if (comparable.compareTo(right) >= 0) {
            throw new IllegalArgumentException("Left range boundary must be less than right");
        }
    }

    private void reset() {
        init = null;
        exit = null;
        transitions.clear();
        unconditionals.clear();
        states.clear();
    }

    private void checkState(S state) {
        Objects.requireNonNull(state);
        if (!stateType.isAssignableFrom(state.getClass())) {
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
        this.unconditionals.remove(state);
        this.unconditionals.values().remove(state);
        return this;
    }

    private void addConditionalTransition(S from, S to, Token<T> token) {
        checkState(from);
        checkState(to);
        var map = transitions.get(from);
        if (map == null) {
            throw new InvalidStateException("Required source state cannot have outgoing transitions", from);
        }
        if (!Objects.equals(exit, to) && !states.contains(to)) {
            throw new InvalidStateException("Required target state not found", to);
        }
        var transition = new Transition<>(to, token, TransitionType.CONDITIONAL);
        map.put(to, transition);
    }

    /**
     * Adds a new conditional transition to the finite state machine.
     *
     * @param from   source state key
     * @param to     target state key
     * @param tokens token values
     * @return this instance of {@link MachineModelBuilder}
     */
    @SafeVarargs
    public final MachineModelBuilder<S, T> addTransition(S from, S to, T... tokens) {
        if (tokens == null) {
            addConditionalTransition(from, to, new SingleToken<>(null));
            return this;
        }
        if (!tokenType.isAssignableFrom(tokens.getClass().getComponentType())) {
            throw new IllegalArgumentException("The class of the token object is not equal to the expected class");
        }
        var set = Set.of(tokens);
        var token = set.size() == 1 ?
                new SingleToken<>(set.iterator().next())
                : new SetToken<>(set);
        addConditionalTransition(from, to, token);
        return this;
    }

    /**
     * Adds a new conditional transition by range to the finite state machine.
     *
     * @param from  source state key
     * @param to    target state key
     * @param start start range value
     * @param end   end range value
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> addRangeTransition(S from, S to, T start, T end) {
        if (start == null || end == null) {
            throw new IllegalStateException("Range boundaries must be not null");
        }
        if (tokenType == Boolean.class) {
            throw new IllegalStateException("Boolean values cannot be used in range checks");
        }
        if (!Comparable.class.isAssignableFrom(tokenType)) {
            throw new IllegalStateException("Types that do not implement the Comparable " +
                    "interface cannot participate in range checks");
        }
        checkRange(start, end);
        addConditionalTransition(from, to, new RangeToken<>(start, end));
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
        checkState(from);
        checkState(to);
        if (!transitions.containsKey(from)) {
            throw new InvalidStateException("Required source state cannot have outgoing transitions", from);
        }
        if (!Objects.equals(exit, to) && !states.contains(to)) {
            throw new InvalidStateException("Required target state not found", to);
        }
        unconditionals.put(from, to);
        return this;
    }

    private void removeConditionalTransition(S from, S to) {
        var map = this.transitions.get(from);
        if (map == null) {
            return;
        }
        map.remove(to);
    }

    /**
     * Removes a transition from a finite state machine.
     *
     * @param from source state key
     * @param to   target state key
     * @param type transition type
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> removeTransition(S from, S to, TransitionType type) {
        checkState(from);
        checkState(to);
        if (type == null) {
            removeConditionalTransition(from, to);
            unconditionals.remove(from);
        } else if (type == TransitionType.CONDITIONAL) {
            removeConditionalTransition(from, to);
        } else {
            unconditionals.remove(from);
        }
        return this;
    }

    /**
     * Removes transitions of all types from a finite state machine.
     *
     * @param from source state key
     * @param to   target state key
     * @return this instance of {@link MachineModelBuilder}
     */
    public MachineModelBuilder<S, T> removeTransition(S from, S to) {
        return removeTransition(from, to, null);
    }

    private State<S, T> createState(S state) {
        var transitions = this.transitions.get(state);
        var unconditional = this.unconditionals.get(state);
        if (unconditional == null) {
            return new State<>(state, transitions, null);
        }
        var transition = new Transition<S, T>(unconditional, null, TransitionType.UNCONDITIONAL);
        return new State<>(state, transitions, transition);
    }

    /**
     * Completes the build of the finite state machine model and returns the result.
     *
     * @return built finite state machine model
     */
    public MachineModel<S, T> build() {
        Objects.requireNonNull(init);
        Objects.requireNonNull(exit);
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
        var ret = new MachineModel<>(stateType, tokenType, init, exit, states);
        this.reset();
        return ret;
    }
}
