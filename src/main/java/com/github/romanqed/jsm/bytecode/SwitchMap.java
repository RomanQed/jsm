package com.github.romanqed.jsm.bytecode;

import org.objectweb.asm.MethodVisitor;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

abstract class SwitchMap<T> {
    protected final Set<T> body;

    protected SwitchMap() {
        this.body = new HashSet<>();
    }

    void put(T key) {
        if (body.contains(key)) {
            throw new IllegalArgumentException("Duplicate key found");
        }
        body.add(key);
    }

    void remove(T key) {
        body.remove(key);
    }

    abstract void visitSwitch(MethodVisitor visitor, Consumer<MethodVisitor> def, BiConsumer<MethodVisitor, T> handler);
}
