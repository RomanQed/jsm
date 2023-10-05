package com.github.romanqed.jsm.model;

/**
 *
 */
public interface TokenVisitor {

    /**
     *
     * @param token
     * @param <T>
     */
    <T> void visit(SingleToken<T> token);

    /**
     *
     * @param token
     * @param <T>
     */
    <T> void visit(RangeToken<T> token);

    /**
     *
     * @param token
     * @param <T>
     */
    <T> void visit(SetToken<T> token);
}
