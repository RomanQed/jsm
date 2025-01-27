package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

public interface Comparator {

    void compare(MethodVisitor visitor, Object expected, Consumer<MethodVisitor> then);
}
