package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

/**
 * An interface describing an abstract comparator.
 */
public interface Comparator {

    /**
     * Creates the bytecode for the expression of comparison.
     *
     * @param visitor the method visitor
     * @param then    the handler for a successful branch
     */
    void compare(MethodVisitor visitor, Consumer<MethodVisitor> then);
}
