package com.github.romanqed.switchgen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

final class TableSwitchMap<T> extends AbstractSwitchMap<T> {
    private final int min;
    private final int max;

    TableSwitchMap(Map<Integer, List<T>> hashes, Comparator comparator, int min, int max) {
        super(hashes, comparator);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Supplier<IntStream> getKeysSupplier() {
        return () -> IntStream.range(min, max + 1);
    }

    @Override
    protected void visitSwitch(MethodVisitor visitor, Label defaultLabel, Label[] labels) {
        visitor.visitTableSwitchInsn(min, max, defaultLabel, labels);
    }
}
