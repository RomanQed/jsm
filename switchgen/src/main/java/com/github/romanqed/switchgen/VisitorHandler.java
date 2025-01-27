package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface VisitorHandler {

    void handle(MethodVisitor visitor);
}
