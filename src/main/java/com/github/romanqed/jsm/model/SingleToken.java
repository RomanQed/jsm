package com.github.romanqed.jsm.model;

import java.util.Collections;
import java.util.Objects;

/**
 * A class representing a single value token.
 *
 * @param <T> token value type
 */
public final class SingleToken<T> implements Token<T> {
    private final T value;

    SingleToken(T value) {
        this.value = value;
    }

    /**
     * Returns token value.
     *
     * @return token value
     */
    public T getValue() {
        return value;
    }

    @Override
    public Iterable<T> getValues() {
        return Collections.singletonList(value);
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleToken)) return false;
        var that = (SingleToken<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String format() {
        return value.toString();
    }
}
