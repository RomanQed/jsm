package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

public interface DefaultHandler {

    void handle(MethodVisitor visitor);
}
