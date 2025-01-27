package com.github.romanqed.switchgen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public abstract class AbstractSwitchMap<T> implements SwitchMap<T> {
    protected final Map<Integer, List<T>> hashes;
    protected final Comparator comparator;

    AbstractSwitchMap(Map<Integer, List<T>> hashes, Comparator comparator) {
        this.hashes = hashes;
        this.comparator = comparator;
    }

    protected abstract Supplier<IntStream> getKeysSupplier();

    protected abstract void visitSwitch(MethodVisitor visitor, Label defaultLabel, Label[] labels);

    @Override
    public void visit(MethodVisitor visitor, DefaultHandler defaultHandler, BranchHandler<T> branchHandler) {
        // Get keys
        var supplier = getKeysSupplier();
        // Prepare labels
        var defaultLabel = new Label();
        var length = supplier.get().count();
        var labels = new Label[(int) length];
        Arrays.setAll(labels, i -> new Label());
        // Visit switch insn
        visitSwitch(visitor, defaultLabel, labels);
        // Iterate over keys
        var iterator = supplier.get().iterator();
        var count = 0;
        while (iterator.hasNext()) {
            // Process label
            visitor.visitLabel(labels[count++]);
            var key = iterator.nextInt();
            var values = hashes.get(key);
            // Handle no collision case
            if (values.size() == 1) {
                branchHandler.handle(visitor, values.get(0));
                continue;
            }
            // Handle collision cases
            for (var value : values) {
                // Stack: ..., actual
                visitor.visitInsn(Opcodes.DUP); // -> actual, actualDup
                comparator.compare(visitor, value, v -> branchHandler.handle(v, value)); // -> actual
            }
            // If there is no any compared collision, goto default label
            visitor.visitJumpInsn(Opcodes.GOTO, defaultLabel);
        }
        // Process default label
        visitor.visitLabel(defaultLabel);
        defaultHandler.handle(visitor);
    }
}
