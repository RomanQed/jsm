package com.github.romanqed.switchgen;

import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class Main2 {

    //        Table switch - 0, 1, 2, 3, 4, 5, ...
    //        switch (0) {
    //            case 0:
    //            case 1:
    //            case 2:
    //        }
    //
    //        Lookup switch
    //        switch (0) {
    //            case 1:
    //            case 17:
    //            case 21:
    //        }
    //
    //        Switch-case => key -> action, default -> action
    public static void main(String[] args) throws IOException {
        var c1 = "AaAa";
        var c2 = "BBBB";
        var c3 = "AaBB";
        var c4 = "BBAa";
        var s1 = "Hello";
        var set = Set.of(c1, c2, c3, c4, s1);
        var hashes = calculateHashes(set);
        System.out.println(hashes);
        /*
        1. Если нет коллизии - case hash:
        2. Если коллизия есть - case hash:
                                    if token == c1:
                                    if token == c2:
                                    ...
         */
        var keys = hashes.keySet()
                .stream()
                .sorted()
                .mapToInt(Integer::intValue)
                .toArray();
        var writer = createWriter();
        var mv = createVisitor(writer);
        mv.visitCode();
        var local = mv.newLocal(Type.getType(String.class));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitIntInsn(Opcodes.BIPUSH, 0);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitVarInsn(Opcodes.ASTORE, local);
        //
        //
        mv.visitVarInsn(Opcodes.ALOAD, local);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "hashCode",
                "()I",
                false
        );
        var def = new Label();
        var labels = new Label[keys.length];
        Arrays.setAll(labels, e -> new Label());
        mv.visitLookupSwitchInsn(def, keys, labels);
        var count = 0;
        for (var key : keys) {
            mv.visitLabel(labels[count++]);
            var vls = hashes.get(key);
            // Handle no collision case
            if (vls.size() == 1) {
                visitVal(mv, vls.get(0));
                continue;
            }
            // Handle collision case
            for (var val : vls) {
                visitCmp(
                        mv,
                        val,
                        v -> v.visitVarInsn(Opcodes.ALOAD, local),
                        v -> visitVal(v, val)
                );
            }
        }
        mv.visitLabel(def);
        visitDef(mv);
        //
        //
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        writer.visitEnd();
        var bytes = writer.toByteArray();
        var out = new FileOutputStream("Main.class");
        out.write(bytes);
        out.close();
    }

    static void visitCmp(MethodVisitor mv,
                         String expected,
                         Consumer<MethodVisitor> loader,
                         Consumer<MethodVisitor> body) {
        var out = new Label();
        mv.visitLdcInsn(expected);
        loader.accept(mv);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "equals",
                "(Ljava/lang/Object;)Z",
                false
        );
        mv.visitJumpInsn(Opcodes.IFEQ, out);
        body.accept(mv);
        mv.visitLabel(out);
    }

    static void visitVal(MethodVisitor mv, String val) {
        mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
        );
        mv.visitLdcInsn(val);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false
        );
        mv.visitInsn(Opcodes.RETURN);
    }

    static void visitDef(MethodVisitor mv) {
        mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
        );
        mv.visitLdcInsn("Default");
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false
        );
        mv.visitInsn(Opcodes.RETURN);
    }

    static <T> Map<Integer, List<T>> calculateHashes(Set<T> keys) {
        var ret = new HashMap<Integer, List<T>>();
        for (var key : keys) {
            var hash = key.hashCode();
            var list = ret.computeIfAbsent(hash, k -> new LinkedList<>());
            list.add(key);
        }
        return ret;
    }

    static LocalVariablesSorter createVisitor(LocalVariablesWriter writer) {
        return writer.visitMethodWithLocals(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );
    }

    static LocalVariablesWriter createWriter() {
        var ret = new LocalVariablesWriter(ClassWriter.COMPUTE_FRAMES);
        ret.visit(
                Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "Main",
                null,
                "java/lang/Object",
                null
        );
        return ret;
    }
}
