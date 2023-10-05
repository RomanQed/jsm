package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jsm.model.RangeToken;
import com.github.romanqed.jsm.model.SetToken;
import com.github.romanqed.jsm.model.SingleToken;
import com.github.romanqed.jsm.model.TokenVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

final class BytecodeVisitor implements TokenVisitor {
    private final MethodVisitor visitor;
    private final Label out;
    private final int index;

    BytecodeVisitor(MethodVisitor visitor, Label out, int index) {
        this.visitor = visitor;
        this.out = out;
        this.index = index;
    }

    @Override
    public <T> void visit(SingleToken<T> token) {

    }

    @Override
    public <T> void visit(RangeToken<T> token) {

    }

    @Override
    public <T> void visit(SetToken<T> token) {

    }
}
