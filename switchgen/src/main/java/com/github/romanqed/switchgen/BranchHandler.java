package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface BranchHandler<T> {

    void handle(MethodVisitor visitor, T value);
}
