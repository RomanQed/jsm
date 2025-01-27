package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

public interface SwitchMap<T> {

    void visit(MethodVisitor visitor, LoadHandler loader, DefaultHandler handler, BranchHandler<T> branchHandler);
}
