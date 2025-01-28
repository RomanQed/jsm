package com.github.romanqed.jsm.model;

import java.util.Set;

/**
 * A class describing a token represented by an unordered set of values in the form [a, b, c, d, ...].
 *
 * @param <T> set value type
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
        var that = (SetToken<?>) o;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "SetToken{" +
                "values=" + values +
                '}';
    }

    @Override
    public String format() {
        var builder = new StringBuilder();
        for (var value : values) {
            builder.append(value);
        }
        return builder.toString();
    }
}
