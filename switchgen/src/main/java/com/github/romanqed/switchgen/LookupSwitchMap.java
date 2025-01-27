package com.github.romanqed.switchgen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class LookupSwitchMap<T> extends AbstractSwitchMap<T> {
    private final int[] keys;

    public LookupSwitchMap(Map<Integer, List<T>> hashes, Comparator comparator, int[] keys) {
        super(hashes, comparator);
        this.keys = keys;
    }

    @Override
    protected Supplier<IntStream> getKeysSupplier() {
        return () -> IntStream.of(keys);
    }

    @Override
    protected void visitSwitch(MethodVisitor visitor, Label defaultLabel, Label[] labels) {
        visitor.visitLookupSwitchInsn(defaultLabel, keys, labels);
    }
}
