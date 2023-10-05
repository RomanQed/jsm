package com.github.romanqed.jsm.model;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @param <T>
 */
public final class SingleToken<T> implements Token<T> {
    private final T value;

    SingleToken(T value) {
        this.value = value;
    }

    /**
     *
     * @return
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
        return Objects.hash(value);
    }
}
