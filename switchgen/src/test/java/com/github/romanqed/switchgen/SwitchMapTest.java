package com.github.romanqed.switchgen;

import com.github.romanqed.jeflect.loader.DefineClassLoader;
import com.github.romanqed.jeflect.loader.DefineLoader;
import com.github.romanqed.jeflect.loader.DefineObjectFactory;
import com.github.romanqed.jeflect.loader.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SwitchMapTest {
    private static final DefineLoader LOADER = new DefineClassLoader();
    private static final ObjectFactory<Object> OBJECT_FACTORY = new DefineObjectFactory<>(LOADER);

    @SuppressWarnings("unchecked")
    private static <T, K> T generateImpl(Class<T> type,
                                         int num,
                                         Consumer<MethodVisitor> hasher,
                                         String arg,
                                         int load,
                                         int ret,
                                         SwitchMap<K> map,
                                         K def) {
        var name = type.getSimpleName() + "Impl" + num;
        var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(
                Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(type)}
        );
        var cv = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );
        cv.visitCode();
        cv.visitVarInsn(Opcodes.ALOAD, 0);
        cv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false
        );
        cv.visitInsn(Opcodes.RETURN);
        cv.visitMaxs(0, 0);
        cv.visitEnd();
        var mv = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "map",
                "(" + arg + ")" + arg,
                null,
                null
        );
        mv.visitCode();
        mv.visitVarInsn(load, 1);
        hasher.accept(mv);
        map.visit(
                mv,
                v -> v.visitVarInsn(load, 1),
                v -> {
                    v.visitLdcInsn(def);
                    v.visitInsn(ret);
                },
                (v, e) -> {
                    v.visitLdcInsn(e);
                    v.visitInsn(ret);
                });
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        writer.visitEnd();
        var bytes = writer.toByteArray();
        return (T) OBJECT_FACTORY.create(name, () -> bytes);
    }

    private static void hashPrim(MethodVisitor mv, String owner, String d) {
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                owner,
                "hashCode",
                d,
                false
        );
    }

    private static void hashLong(MethodVisitor mv) {
        hashPrim(mv, "java/lang/Long", "(J)I");
    }

    private static void hashDouble(MethodVisitor mv) {
        hashPrim(mv, "java/lang/Double", "(D)I");
    }

    private static void hashString(MethodVisitor mv) {
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/util/Objects",
                "hashCode",
                "(Ljava/lang/Object;)I",
                false
        );
    }

    @Test
    public void testLongTableMap() {
        var keys = Set.of(11L, 12L, 13L, 15L);
        var map = SwitchMaps.createTable(keys);
        var o = generateImpl(
                LM.class,
                0,
                SwitchMapTest::hashLong,
                "J",
                Opcodes.LLOAD,
                Opcodes.LRETURN,
                map,
                -1L
        );
        assertEquals(11L, o.map(11L));
        assertEquals(12L, o.map(12L));
        assertEquals(13L, o.map(13L));
        assertEquals(15L, o.map(15L));
        assertEquals(-1L, o.map(1L));
        assertEquals(-1L, o.map(10L));
    }

    @Test
    public void testLongLookupMap() {
        var keys = Set.of(11L, 12L, 13L, 15L);
        var map = SwitchMaps.createLookup(keys);
        var o = generateImpl(
                LM.class,
                1,
                SwitchMapTest::hashLong,
                "J",
                Opcodes.LLOAD,
                Opcodes.LRETURN,
                map,
                -1L
        );
        assertEquals(11L, o.map(11L));
        assertEquals(12L, o.map(12L));
        assertEquals(13L, o.map(13L));
        assertEquals(15L, o.map(15L));
        assertEquals(-1L, o.map(1L));
        assertEquals(-1L, o.map(10L));
    }

    @Test
    public void testDoubleLookupMap() {
        var keys = Set.of(11D, 12D, 13D, 15D);
        var map = SwitchMaps.createLookup(keys);
        var o = generateImpl(
                DM.class,
                0,
                SwitchMapTest::hashDouble,
                "D",
                Opcodes.DLOAD,
                Opcodes.DRETURN,
                map,
                -1D
        );
        assertEquals(11D, o.map(11D));
        assertEquals(12D, o.map(12D));
        assertEquals(13D, o.map(13D));
        assertEquals(15D, o.map(15D));
        assertEquals(-1D, o.map(1D));
        assertEquals(-1D, o.map(10D));
    }

    @Test
    public void testStringTableMap() {
        var keys = Set.of("1", "2", "3", "9");
        var map = SwitchMaps.createTable(keys);
        var o = generateImpl(
                SM.class,
                0,
                SwitchMapTest::hashString,
                "Ljava/lang/String;",
                Opcodes.ALOAD,
                Opcodes.ARETURN,
                map,
                "default"
        );
        assertEquals("1", o.map("1"));
        assertEquals("2", o.map("2"));
        assertEquals("3", o.map("3"));
        assertEquals("9", o.map("9"));
        assertEquals("default", o.map(null));
        assertEquals("default", o.map("5"));
        assertEquals("default", o.map("6"));
        assertEquals("default", o.map("7"));
        assertEquals("default", o.map("8"));
    }

    @Test
    public void testStringLookupMap() {
        var keys = Set.of("1", "2", "3", "9");
        var map = SwitchMaps.createLookup(keys);
        var o = generateImpl(
                SM.class,
                1,
                SwitchMapTest::hashString,
                "Ljava/lang/String;",
                Opcodes.ALOAD,
                Opcodes.ARETURN,
                map,
                "default"
        );
        assertEquals("1", o.map("1"));
        assertEquals("2", o.map("2"));
        assertEquals("3", o.map("3"));
        assertEquals("9", o.map("9"));
        assertEquals("default", o.map(null));
        assertEquals("default", o.map("5"));
        assertEquals("default", o.map("6"));
        assertEquals("default", o.map("7"));
        assertEquals("default", o.map("8"));
    }

    @Test
    public void testStringLookupMapWithCollisions() {
        var keys = Set.of(
                "Unique",
                "Hi",
                "AaAa",
                "BBBB",
                "AaBB",
                "BBAa"
        );
        var map = SwitchMaps.createLookup(keys);
        var o = generateImpl(
                SM.class,
                2,
                SwitchMapTest::hashString,
                "Ljava/lang/String;",
                Opcodes.ALOAD,
                Opcodes.ARETURN,
                map,
                "default"
        );
        assertEquals("Unique", o.map("Unique"));
        assertEquals("Hi", o.map("Hi"));
        assertEquals("AaAa", o.map("AaAa"));
        assertEquals("BBBB", o.map("BBBB"));
        assertEquals("AaBB", o.map("AaBB"));
        assertEquals("BBAa", o.map("BBAa"));
        assertEquals("default", o.map(null));
        assertEquals("default", o.map("AAAA"));
        assertEquals("default", o.map("RANDOM"));
        assertEquals("default", o.map("Test"));
        assertEquals("default", o.map("123"));
    }

    public interface LM {

        long map(long val);
    }

    public interface DM {

        double map(double val);
    }

    public interface SM {

        String map(String val);
    }
}
