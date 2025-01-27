package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SwitchMap<T> {

    void visit(MethodVisitor visitor,
               Consumer<MethodVisitor> loader,
               Consumer<MethodVisitor> defaultHandler,
               BiConsumer<MethodVisitor, T> branchHandler);
}
