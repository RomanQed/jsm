package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface DefaultHandler {

    void handle(MethodVisitor visitor);
}
