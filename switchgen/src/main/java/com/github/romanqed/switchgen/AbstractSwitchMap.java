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

    protected AbstractSwitchMap(Map<Integer, List<T>> hashes, Comparator comparator) {
        this.hashes = hashes;
        this.comparator = comparator;
    }

    protected abstract Supplier<IntStream> getKeysSupplier();

    protected abstract void visitSwitch(MethodVisitor visitor, Label defaultLabel, Label[] labels);

    @Override
    public void visit(MethodVisitor visitor,
                      LoadHandler loader,
                      DefaultHandler handler,
                      BranchHandler<T> branchHandler) {
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
            // If there is no such value, goto default label
            if (values == null) {
                visitor.visitJumpInsn(Opcodes.GOTO, defaultLabel);
                continue;
            }
            // Handle no collision case
            if (values.size() == 1) {
                branchHandler.handle(visitor, values.get(0));
                continue;
            }
            // Handle collision cases
            for (var value : values) {
                // Load actual value to compare with expected
                loader.handle(visitor);
                comparator.compare(visitor, value, v -> branchHandler.handle(v, value));
            }
            // If there is no any compared collision, goto default label
            visitor.visitJumpInsn(Opcodes.GOTO, defaultLabel);
        }
        // Process default label
        visitor.visitLabel(defaultLabel);
        handler.handle(visitor);
    }
}
