package com.github.romanqed.jsm.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

final class TableSwitchMap<T> extends SwitchMap<T> {
    private final Function<Integer, T> mapper;
    private final int start;
    private final int end;

    TableSwitchMap(Function<Integer, T> mapper, int start, int end) {
        this.mapper = mapper;
        this.start = start;
        this.end = end;
    }

    @Override
    void visitSwitch(MethodVisitor visitor, Consumer<MethodVisitor> def, BiConsumer<MethodVisitor, T> handler) {
        var labels = new Label[body.size()];
        Arrays.setAll(labels, e -> new Label());
        var defaultLabel = new Label();
        visitor.visitTableSwitchInsn(start, end, defaultLabel, labels);
        for (var i = 0; i < labels.length; ++i) {
            visitor.visitLabel(labels[i]);
            var object = mapper.apply(start + i);
            handler.accept(visitor, object);
        }
        visitor.visitLabel(defaultLabel);
        def.accept(visitor);
    }
}
