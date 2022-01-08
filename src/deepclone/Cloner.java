package deepclone;

import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

public class Cloner {
    private static final Set<Class<?>> wrapperTypes = new HashSet<>();

    static {
        wrapperTypes.add(Boolean.class);
        wrapperTypes.add(Character.class);
        wrapperTypes.add(Byte.class);
        wrapperTypes.add(Short.class);
        wrapperTypes.add(Integer.class);
        wrapperTypes.add(Long.class);
        wrapperTypes.add(Float.class);
        wrapperTypes.add(Double.class);
        wrapperTypes.add(Void.class);
    }

    public static <T> T deepClone(T original) throws IllegalAccessException {
        if (original == null) {
            return null;
        }

        Class<?> c = original.getClass();

        if (isImmutable(c)) {
            return original;
        }

        return deepCloneObject(original, c);
    }

    private static boolean isImmutable(Class<?> c) {
        return c.isPrimitive() ||
            wrapperTypes.contains(c) ||
            c == String.class ||
            c.isRecord();
    }

    private static <T> T deepCloneObject(T original, Class<?> c)
            throws IllegalAccessException
    {
        ObjenesisStd objenesis = new ObjenesisStd();

        @SuppressWarnings("unchecked")
        T clone = (T)objenesis.newInstance(c);

        //@see ClonerTests.NestedObjectWithTwoSameReferences
        IdentityHashMap<Object, Object> originalToCloned = new IdentityHashMap<>();

        while (c != null) {
            Field[] fields = c.getDeclaredFields();

            for (Field f : fields) {
                f.setAccessible(true);

                Object originalValue = f.get(original);
                Object clonedValue = originalToCloned.get(originalValue);

                if (clonedValue == null) {
                    clonedValue = deepClone(originalValue);
                    originalToCloned.put(originalValue, clonedValue);
                }

                f.set(clone, clonedValue);
            }

            c = c.getSuperclass();
        }

        return clone;
    }
}