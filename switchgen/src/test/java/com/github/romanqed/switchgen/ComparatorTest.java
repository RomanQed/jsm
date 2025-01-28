package com.github.romanqed.switchgen;

import com.github.romanqed.jeflect.loader.DefineClassLoader;
import com.github.romanqed.jeflect.loader.DefineLoader;
import com.github.romanqed.jeflect.loader.DefineObjectFactory;
import com.github.romanqed.jeflect.loader.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ComparatorTest {
    private static final DefineLoader LOADER = new DefineClassLoader();
    private static final ObjectFactory<Object> OBJECT_FACTORY = new DefineObjectFactory<>(LOADER);

    @SuppressWarnings("unchecked")
    private static <T> T generateImpl(Class<T> type, String arg, boolean l, int opcode, Comparator comparator) {
        var name = type.getSimpleName() + "Impl";
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
                "eq",
                "(" + arg + arg + ")Z",
                null,
                null
        );
        mv.visitCode();
        mv.visitVarInsn(opcode, 1);
        mv.visitVarInsn(opcode, l ? 3 : 2);
        comparator.compare(mv, v -> {
            v.visitInsn(Opcodes.ICONST_1);
            v.visitInsn(Opcodes.IRETURN);
        });
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        writer.visitEnd();
        var bytes = writer.toByteArray();
        return (T) OBJECT_FACTORY.create(name, () -> bytes);
    }

    @Test
    public void testLongComparator() {
        var cmp = generateImpl(LC.class, "J", true, Opcodes.LLOAD, new LongComparator());
        assertTrue(cmp.eq(0, 0));
        assertTrue(cmp.eq(1, 1));
        assertTrue(cmp.eq(Long.MIN_VALUE, Long.MIN_VALUE));
        assertTrue(cmp.eq(Long.MAX_VALUE, Long.MAX_VALUE));
        assertFalse(cmp.eq(0, 1));
        assertFalse(cmp.eq(1, 0));
        assertFalse(cmp.eq(Long.MIN_VALUE, Long.MAX_VALUE));
        assertFalse(cmp.eq(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    @Test
    public void testDoubleComparator() {
        var cmp = generateImpl(DC.class, "D", true, Opcodes.DLOAD, new DoubleComparator());
        assertTrue(cmp.eq(0, 0));
        assertTrue(cmp.eq(1, 1));
        assertTrue(cmp.eq(Double.MIN_VALUE, Double.MIN_VALUE));
        assertTrue(cmp.eq(Double.MAX_VALUE, Double.MAX_VALUE));
        assertFalse(cmp.eq(0, 1));
        assertFalse(cmp.eq(1, 0));
        assertFalse(cmp.eq(Double.MIN_VALUE, Double.MAX_VALUE));
        assertFalse(cmp.eq(Double.MAX_VALUE, Double.MIN_VALUE));
    }

    @Test
    public void testStringComparator() {
        var cmp = generateImpl(SC.class, "Ljava/lang/String;", false, Opcodes.ALOAD, new StringComparator());
        assertTrue(cmp.eq("0", "0"));
        assertTrue(cmp.eq("1", "1"));
        assertTrue(cmp.eq("abcdef", "abcdef"));
        assertTrue(cmp.eq("hello, world", "hello, world"));
        assertFalse(cmp.eq("0", "1"));
        assertFalse(cmp.eq("1", "0"));
        assertFalse(cmp.eq("abcdef", "hello"));
        assertFalse(cmp.eq("Hello", "abcdef"));
    }

    public interface LC {

        boolean eq(long a, long b);
    }

    public interface DC {

        boolean eq(double a, double b);
    }

    public interface SC {

        boolean eq(String a, String b);
    }
}
