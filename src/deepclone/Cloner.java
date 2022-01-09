package deepclone;

import org.objenesis.ObjenesisStd;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Set;

public class Cloner {
    private static final Set<Class<?>> knownImmutableTypes = Set.of(
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        Void.class,
        String.class
    );

    private final IdentityHashMap<Object, Object> clonesMap =
        new IdentityHashMap<>();

    public <T> T deepClone(T original) throws IllegalAccessException {
        if (original == null) {
            return null;
        }

        Class<?> c = original.getClass();

        if (isImmutable(c)) {
            return original;
        }

        Object clone = clonesMap.get(original);

        if (clone == null) {
            clone = (c.isArray())
                ? deepCloneArray(original, c)
                : deepCloneObject(original, c);
        }

        @SuppressWarnings("unchecked")
        T tClone = (T)clone;

        return tClone;
    }

    private static boolean isImmutable(Class<?> c) {
        return c.isPrimitive() ||
            knownImmutableTypes.contains(c) ||
            c.isRecord();
    }

    private Object deepCloneArray(Object original, Class<?> c)
            throws IllegalAccessException
    {
        int length = Array.getLength(original);
        Object clone = Array.newInstance(c.componentType(), length);

        clonesMap.put(original, clone);

        for (int i = 0; i < length; ++i) {
            Object value = Array.get(original, i);
            Array.set(clone, i, deepClone(value));
        }

        return clone;
    }

    private Object deepCloneObject(Object original, Class<?> c)
            throws IllegalAccessException
    {
        ObjenesisStd objenesis = new ObjenesisStd();
        Object clone = objenesis.newInstance(c);

        clonesMap.put(original, clone);

        while (c != null) {
            Field[] fields = c.getDeclaredFields();

            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                f.setAccessible(true);

                Object value = f.get(original);
                f.set(clone, deepClone(value));
            }

            c = c.getSuperclass();
        }

        return clone;
    }
}
