package com.github.romanqed.jsm.model;

import java.util.Objects;

/**
 * A class describing a final state machine transition.
 *
 * @param <S> state type
 * @param <T> token type
 */
public final class Transition<S, T> implements Formattable {
    private final S target;
    private final Token<T> token;
    private final TransitionType type;

    Transition(S target, Token<T> token, TransitionType type) {
        this.target = target;
        this.token = token;
        this.type = type;
    }

    /**
     * Returns the state that the transition leads to.
     *
     * @return the state that the transition leads to
     */
    public S getTarget() {
        return target;
    }

    /**
     * Returns the token that triggers the transition.
     *
     * @return the token that triggers the transition
     */
    public Token<T> getToken() {
        return token;
    }

    /**
     * Returns the transition type.
     *
     * @return the transition type
     */
    public TransitionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (Transition<?, ?>) o;
        return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return target == null ? 0 : target.hashCode();
    }

    @Override
    public String toString() {
        return "Transition{" +
                "target=" + target +
                ", token=" + token +
                ", type=" + type +
                '}';
    }

    @Override
    public String format() {
        var builder = new StringBuilder();
        if (target != null) {
            builder.append(target);
        }
        if (type == TransitionType.CONDITIONAL) {
            builder.append(token.format());
        }
        return builder.toString();
    }
}
