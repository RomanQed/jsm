package com.github.romanqed.jsm.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

final class LookupSwitchMap<T> extends SwitchMap<T> {
    private final Function<T, Integer> mapper;

    LookupSwitchMap(Function<T, Integer> mapper) {
        this.mapper = mapper;
    }

    @Override
    void visitSwitch(MethodVisitor visitor, Consumer<MethodVisitor> def, BiConsumer<MethodVisitor, T> handler) {
        var data = new LinkedList<Entry<T>>();
        this.body.forEach(value -> data.add(new Entry<>(mapper.apply(value), value)));
        data.sort(Comparator.comparingInt(e -> e.key));
        var keys = data.stream()
                .mapToInt(e -> e.key)
                .toArray();
        var defaultLabel = new Label();
        var labels = new Label[data.size()];
        Arrays.setAll(labels, e -> new Label());
        visitor.visitLookupSwitchInsn(defaultLabel, keys, labels);
        var count = 0;
        for (var entry : data) {
            visitor.visitLabel(labels[count++]);
            handler.accept(visitor, entry.value);
        }
        visitor.visitLabel(defaultLabel);
        def.accept(visitor);
    }

    private static final class Entry<T> {
        private final int key;
        private final T value;

        private Entry(int key, T value) {
            this.key = key;
            this.value = value;
        }

        public int getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }
    }
}
