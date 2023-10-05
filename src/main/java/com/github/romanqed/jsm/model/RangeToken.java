package com.github.romanqed.jsm.model;

import java.util.List;
import java.util.Objects;

/**
 *
 * @param <T>
 */
public final class RangeToken<T> implements Token<T> {
    private final T start;
    private final T end;

    RangeToken(T start, T end) {
        this.start = start;
        this.end = end;
    }

    /**
     *
     * @return
     */
    public T getStart() {
        return start;
    }

    /**
     *
     * @return
     */
    public T getEnd() {
        return end;
    }

    @Override
    public Iterable<T> getValues() {
        return List.of(start, end);
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RangeToken)) return false;
        var that = (RangeToken<?>) o;
        return start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "RangeToken{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
