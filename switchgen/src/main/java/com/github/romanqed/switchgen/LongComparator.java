package com.github.romanqed.switchgen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

public final class LongComparator implements Comparator {

    @Override
    public void compare(MethodVisitor visitor, Object expected, Consumer<MethodVisitor> then) {
        // Stack: actualLong1, actualLong2
        visitor.visitLdcInsn(expected);
        // From opcodes docs:
        // If value1 is equal to value2, the int value 0 is pushed onto the operand stack.
        visitor.visitInsn(Opcodes.LCMP);
        var exit = new Label();
        // Jump out if comparison result != 0
        visitor.visitJumpInsn(Opcodes.IFNE, exit); // if (actual == expected) {
        then.accept(visitor); // then();
        // }
        visitor.visitLabel(exit);
    }
}
