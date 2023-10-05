package com.github.romanqed.jsm.model;

/**
 *
 * @param <T>
 */
public interface Token<T> {

    /**
     *
     * @param visitor
     */
    void accept(TokenVisitor visitor);

    /**
     *
     * @return
     */
    Iterable<T> getValues();
}
