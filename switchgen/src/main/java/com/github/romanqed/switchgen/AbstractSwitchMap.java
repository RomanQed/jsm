package com.github.romanqed.switchgen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * The skeleton implementation of the {@link SwitchMap}.
 * Contains common implementation of {@link SwitchMap#visit(MethodVisitor, Consumer, Consumer, BiConsumer)} method.
 *
 * @param <T> the type of the switch-case argument
 */
public abstract class AbstractSwitchMap<T> implements SwitchMap<T> {
    /**
     * Hashes of the case values with collisions.
     */
    protected final Map<Integer, List<T>> hashes;
    /**
     * Comparator for this type. If type has no collisions (<= 4 byte), comparator must be null.
     */
    protected final Comparator comparator;

    /**
     * Constructs {@link AbstractSwitchMap} with given hashes and type comparator.
     *
     * @param hashes     the specified hash mapping, must be non-null
     * @param comparator the specified comparator, must be non-null if type has collisions (> 4 byte), null otherwise
     */
    protected AbstractSwitchMap(Map<Integer, List<T>> hashes, Comparator comparator) {
        this.hashes = hashes;
        this.comparator = comparator;
    }

    /**
     * Returns the supplier of {@link IntStream} containing sorted case entries.
     *
     * @return the supplier of {@link IntStream} containing sorted case entries.
     */
    protected abstract Supplier<IntStream> getKeysSupplier();

    /**
     * Visits the specified switch-case instruction.
     *
     * @param visitor      the method visitor
     * @param defaultLabel the default case label
     * @param labels       the array containing value case labels
     */
    protected abstract void visitSwitch(MethodVisitor visitor, Label defaultLabel, Label[] labels);

    @Override
    public void visit(MethodVisitor visitor,
                      Consumer<MethodVisitor> loader,
                      Consumer<MethodVisitor> defaultHandler,
                      BiConsumer<MethodVisitor, T> branchHandler) {
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
                branchHandler.accept(visitor, values.get(0));
                continue;
            }
            // Handle collision cases
            for (var value : values) {
                // Load expected value
                visitor.visitLdcInsn(value);
                // Load actual value to compare with expected
                loader.accept(visitor);
                comparator.compare(visitor, v -> branchHandler.accept(v, value));
            }
            // If there is no any compared collision, goto default label
            visitor.visitJumpInsn(Opcodes.GOTO, defaultLabel);
        }
        // Process default label
        visitor.visitLabel(defaultLabel);
        defaultHandler.accept(visitor);
    }
}
