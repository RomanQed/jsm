package com.github.romanqed.jsm.model;

/**
 * An interface describing the visitor for the {@link Token}.
 */
public interface TokenVisitor {

    /**
     * Method to be called when visiting {@link SingleToken}.
     *
     * @param token {@link SingleToken} instance
     * @param <T>   single token value type
     */
    <T> void visit(SingleToken<T> token);

    /**
     * Method to be called when visiting {@link RangeToken}.
     *
     * @param token {@link RangeToken} instance
     * @param <T>   range token value type
     */
    <T> void visit(RangeToken<T> token);

    /**
     * Method to be called when visiting {@link SetToken}.
     *
     * @param token {@link SetToken} instance
     * @param <T>   set token value type
     */
    <T> void visit(SetToken<T> token);
}
