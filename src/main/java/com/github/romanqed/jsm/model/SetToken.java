package com.github.romanqed.jsm.model;

import java.util.Objects;
import java.util.Set;

/**
 *
 * @param <T>
 */
public final class SetToken<T> implements Token<T> {
    private final Set<T> values;

    SetToken(Set<T> values) {
        this.values = values;
    }

    @Override
    public Set<T> getValues() {
        return values;
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetToken)) return false;
        SetToken<?> setToken = (SetToken<?>) o;
        return values.equals(setToken.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "SetToken{" +
                "values=" + values +
                '}';
    }
}
