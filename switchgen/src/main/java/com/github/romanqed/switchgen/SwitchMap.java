package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An interface describing an abstract switch case mapping.
 *
 * @param <T> the type of the switch-case argument
 */
public interface SwitchMap<T> {

    /**
     * Creates the full bytecode for the switch case structure.
     *
     * @param visitor        the method visitor
     * @param loader         the loader for the switch argument (for collision solving), may be null
     * @param defaultHandler the handler for a default case, must be not null
     * @param branchHandler  the handler for a value case, must be not null
     */
    void visit(MethodVisitor visitor,
               Consumer<MethodVisitor> loader,
               Consumer<MethodVisitor> defaultHandler,
               BiConsumer<MethodVisitor, T> branchHandler);
}
