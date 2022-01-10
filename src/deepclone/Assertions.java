package deepclone;

import java.lang.reflect.Array;

import static org.junit.jupiter.api.Assertions.*;

public class Assertions {
    public static void assertEqualButNotSame(Object a, Object b) {
        assertEquals(a, b);
        assertNotSame(a, b);
    }

    public static void assertArrayElementsEqualButNotSame(Object a, Object b, int dimensions) {
        int length = Array.getLength(a);
        assertEquals(length, Array.getLength(b));

        if (dimensions == 1) {
            for (int i = 0; i < length; ++i) {
                assertEqualButNotSame(
                    Array.get(a, i),
                    Array.get(b, i)
                );
            }

            return;
        }

        for (int i = 0; i < length; ++i) {
            assertArrayElementsEqualButNotSame(
                Array.get(a, i),
                Array.get(b, i),
                dimensions - 1
            );
        }
    }

    public static <T> void assertIterableElementsEqualButNotSame(
        Iterable<T> a,
        Iterable<T> b
    ) {
        var aIt = a.iterator();
        var bIt = b.iterator();

        while (aIt.hasNext() && bIt.hasNext()) {
            if (!aIt.next().equals(bIt.next())) {
                fail();
            }
        }

        if (aIt.hasNext() || bIt.hasNext()) {
            fail();
        }
    }
}
