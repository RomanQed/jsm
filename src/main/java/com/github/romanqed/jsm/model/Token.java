package com.github.romanqed.jsm.model;

/**
 * Interface describing the universal token of a finite state machine.
 *
 * @param <T> token value type
 */
public interface Token<T> extends Formattable {

    /**
     * Accepts an instance of {@link TokenVisitor} and calls one of its methods
     * depending on the {@link Token} implementation.
     *
     * @param visitor instance of {@link TokenVisitor}
     */
    void accept(TokenVisitor visitor);

    /**
     * Returns token values.
     *
     * @return token values
     */
    Iterable<T> getValues();
}
