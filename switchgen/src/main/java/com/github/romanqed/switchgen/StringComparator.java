package com.github.romanqed.switchgen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

/**
 * Implementation of the {@link Comparator} for the {@link String}. Uses {@link String#equals(Object)} method.
 */
public final class StringComparator implements Comparator {

    @Override
    public void compare(MethodVisitor visitor, Consumer<MethodVisitor> then) {
        visitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "equals",
                "(Ljava/lang/Object;)Z",
                false
        );
        var exit = new Label();
        // Jump out if comparison result is false (0)
        visitor.visitJumpInsn(Opcodes.IFEQ, exit); // if (expected.equals(actual)) {
        then.accept(visitor); // then();
        // }
        visitor.visitLabel(exit);
    }
}
