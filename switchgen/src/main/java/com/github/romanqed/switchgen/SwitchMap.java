package com.github.romanqed.switchgen;

import org.objectweb.asm.MethodVisitor;

public interface SwitchMap<T> {

    void visit(MethodVisitor visitor, DefaultHandler defaultHandler, BranchHandler<T> branchHandler);
}
