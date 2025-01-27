package com.github.romanqed.switchgen;

import com.github.romanqed.jeflect.loader.DefineClassLoader;
import com.github.romanqed.jeflect.loader.DefineLoader;
import com.github.romanqed.jeflect.loader.DefineObjectFactory;
import com.github.romanqed.jeflect.loader.ObjectFactory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class ComparatorTest {
    private static final DefineLoader LOADER = new DefineClassLoader();
    private static final ObjectFactory<Object> OBJECT_FACTORY = new DefineObjectFactory<>(LOADER);

    @SuppressWarnings("unchecked")
    private static <T> T generateImpl(Class<T> type,  String args, Comparator comparator) {
        var name = type.getName() + "Impl";
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(
                Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(type)}
        );
        var mv = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "eq",
                "(" + args + ")Z",
                null,
                null
        );
        mv.visitCode();

        mv.visitMaxs(0, 0);
        mv.visitEnd();
        writer.visitEnd();
        var bytes = writer.toByteArray();
        return (T) OBJECT_FACTORY.create(name, () -> bytes);
    }

    public interface LC {

        boolean eq(long a, long b);
    }

    public interface FC {

        boolean eq(float a, float b);
    }

    public interface DC {

        boolean eq(double a, double b);
    }

    public interface SC {

        boolean eq(String a, String b);
    }
}
