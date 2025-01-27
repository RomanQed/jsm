package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface LoadHandler {

    void handle(MethodVisitor visitor);
}
