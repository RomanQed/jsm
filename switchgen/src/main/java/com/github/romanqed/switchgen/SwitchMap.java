package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

public interface SwitchMap<T> {

    void visit(MethodVisitor visitor, VisitorHandler loader, VisitorHandler handler, BranchHandler<T> branchHandler);
}
