package deepclone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

import static deepclone.Assertions.*;

public class ClonerTests {
    private Cloner cloner;
    
    @BeforeEach
    public void createNewCloner() {
        cloner = new Cloner();
    }
    
    @Nested
    class ImmutableTypesAreNotCloned {
        @Test
        public void test_primitiveTypes() throws IllegalAccessException {
            int a = 42;
            char u = 'u';

            int aClone = cloner.deepClone(a);
            char uClone = cloner.deepClone(u);

            assertSame(a, aClone);
            assertSame(u, uClone);
        }

        @Test
        public void test_wrappersOfPrimitiveTypes() throws IllegalAccessException {
            Integer a = 42;
            Character u = 'u';

            Integer aClone = cloner.deepClone(a);
            Character uClone = cloner.deepClone(u);

            assertSame(a, aClone);
            assertSame(u, uClone);
        }

        @Test
        public void test_string() throws IllegalAccessException {
            String s = "Hello, world";
            String sClone = cloner.deepClone(s);

            assertSame(s, sClone);
        }

        @Test
        public void test_record() throws IllegalAccessException {
            record SomeRecord(int foo, String bar) {}

            SomeRecord r = new SomeRecord(42, "Lorem");
            SomeRecord rClone = cloner.deepClone(r);

            assertSame(r, rClone);
        }
    }

    @Test
    public void test_whenObjectIsNullReturnsNull() throws IllegalAccessException {
        Object original = null;
        Object clone = cloner.deepClone(original);

        assertSame(original, clone);
    }

    @Nested
    class AccessModifiersAndInheritance {
        static class A {
            private int aPrivate = 10;
            protected int aProtected = 10;
            int aPackagePrivate = 10;
            public int aPublic = 10;

            public int getAPrivate() {
                return aPrivate;
            }

            public void setAPrivate(int aPrivate) {
                this.aPrivate = aPrivate;
            }

            public int getAProtected() {
                return aProtected;
            }

            public void setAProtected(int aProtected) {
                this.aProtected = aProtected;
            }

            public int getAPackagePrivate() {
                return aPackagePrivate;
            }

            public void setAPackagePrivate(int aPackagePrivate) {
                this.aPackagePrivate = aPackagePrivate;
            }

            public int getAPublic() {
                return aPublic;
            }

            public void setAPublic(int aPublic) {
                this.aPublic = aPublic;
            }
        }

        static class B extends A {
            private boolean bPrivate = true;
            protected boolean bProtected = true;
            boolean bPackagePrivate = true;
            public boolean bPublic = true;

            public boolean getBPrivate() {
                return bPrivate;
            }

            public void setBPrivate(boolean bPrivate) {
                this.bPrivate = bPrivate;
            }

            public boolean getBProtected() {
                return bProtected;
            }

            public void setBProtected(boolean bProtected) {
                this.bProtected = bProtected;
            }

            public boolean getBPackagePrivate() {
                return bPackagePrivate;
            }

            public void setBPackagePrivate(boolean bPackagePrivate) {
                this.bPackagePrivate = bPackagePrivate;
            }

            public boolean getBPublic() {
                return bPublic;
            }

            public void setBPublic(boolean bPublic) {
                this.bPublic = bPublic;
            }
        }

        static class C extends B {
            private String cPrivate = "Hello";
            protected String cProtected = "Hello";
            String cPackagePrivate = "Hello";
            public String cPublic = "Hello";

            public void setCPrivate(String cPrivate) {
                this.cPrivate = cPrivate;
            }

            public void setCProtected(String cProtected) {
                this.cProtected = cProtected;
            }

            public String getCPrivate() {
                return cPrivate;
            }

            public String getCProtected() {
                return cProtected;
            }

            public String getCPackagePrivate() {
                return cPackagePrivate;
            }

            public void setCPackagePrivate(String cPackagePrivate) {
                this.cPackagePrivate = cPackagePrivate;
            }

            public String getCPublic() {
                return cPublic;
            }

            public void setCPublic(String cPublic) {
                this.cPublic = cPublic;
            }
        }

        @Test
        public void test_ownFields() throws IllegalAccessException {
            testField(C::getCPrivate, C::setCPrivate, "New value");
            testField(C::getCProtected, C::setCProtected, "New value");
            testField(C::getCPackagePrivate, C::setCPackagePrivate, "New value");
            testField(C::getCPublic, C::setCPublic, "New value");
        }

        @Test
        public void test_baseClassFields() throws IllegalAccessException {
            testField(C::getBPrivate, C::setBPrivate, false);
            testField(C::getBProtected, C::setBProtected, false);
            testField(C::getBPackagePrivate, C::setBPackagePrivate, false);
            testField(C::getBPublic, C::setBPublic, false);
        }

        @Test
        public void test_baseClassOfBaseClassFields() throws IllegalAccessException {
            testField(C::getAPrivate, C::setAPrivate, 42);
            testField(C::getAProtected, C::setAProtected, 42);
            testField(C::getAPackagePrivate, C::setAPackagePrivate, 42);
            testField(C::getAPublic, C::setAPublic, 42);
        }

        private <TField> void testField(
            Function<C, TField> getter,
            BiConsumer<C, TField> setter,
            TField newValue
        ) throws IllegalAccessException {
            C original = new C();
            C clone = cloner.deepClone(original);

            assertEquals(getter.apply(original), getter.apply(clone));

            setter.accept(clone, newValue);

            assertNotEquals(getter.apply(original), getter.apply(clone));
        }
    }

    @Nested
    class StaticFieldsAreIgnored {
        static class Foo {
            public static Bar bar = new Bar();
        }

        static class Bar { }

        @Test
        public void test() throws IllegalAccessException {
            Foo original = new Foo();
            Bar barBeforeCloning = Foo.bar;

            cloner.deepClone(original);

            //Если статические поля не игнорировались,
            //то произошло глубокое копирование объекта по
            //ссылке Foo.bar и после клонирование ссылка указывает
            //на другой объект
            assertSame(barBeforeCloning, Foo.bar);
        }
    }

    @Nested
    class FinalFieldsAreCloned {
        static class Foo {
            public final int variable;
            public final int hardcoded = 42;

            public Foo(int variable) {
                this.variable = variable;
            }
        }

        private Foo original;
        private Foo clone;

        @BeforeEach
        public void makeObjects() throws IllegalAccessException {
            original = new Foo(10);
            clone = cloner.deepClone(original);
        }

        @Test
        public void test_variableFields() {
            assertEquals(original.variable, clone.variable);
        }

        @Test
        public void test_hardcodedFields() {
            assertEquals(original.hardcoded, clone.hardcoded);
        }
    }

    @Nested
    class SimpleNestedObject {
        static class Person {
            public String fullName;
            public Address address;

            public Person(String fullName, Address address) {
                this.fullName = fullName;
                this.address = address;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Person other) {
                    return fullName.equals(other.fullName) &&
                        address.equals(other.address);
                }

                return false;
            }
        }

        static class Address {
            public String country;
            public String city;
            public String street;
            public int houseNumber;

            public Address(
                String country,
                String city,
                String street,
                int houseNumber
            ) {
                this.country = country;
                this.city = city;
                this.street = street;
                this.houseNumber = houseNumber;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Address other) {
                    return country.equals(other.country) &&
                        city.equals(other.city) &&
                        street.equals(other.street) &&
                        houseNumber == other.houseNumber;
                }

                return false;
            }
        }

        @Test
        public void test() throws IllegalAccessException {
            Address newYork = new Address("USA", "New York", "Main St.", 42);
            Person bob = new Person("Bob", newYork);

            Person clone = cloner.deepClone(bob);

            //Боб и его клон имеют полностью одинаковые свойства,
            //метод .equals() вернет true
            assertEquals(bob, clone);

            //Но при этом bob и clone - разные объекты
            assertNotSame(bob, clone);

            //bob.address и clone.address тоже указывают на _разные_ объекты
            assertNotSame(bob.address, clone.address);
        }
    }

    @Nested
    class SelfReferencingObject {
        static class Crazy {
            public final Crazy self;

            public Crazy() {
                self = this;
            }
        }

        @Test
        public void test() throws IllegalAccessException {
            Crazy original = new Crazy();
            Crazy clone = cloner.deepClone(original);

            assertNotSame(original, clone);
            assertSame(clone, clone.self);
        }
    }

    @Nested
    class LinkedListWithHeadAndTail {
        static class LinkedList {
            static class Node {
                public int value;
                public Node next;

                public Node(int value, Node next) {
                    this.value = value;
                    this.next = next;
                }
            }

            private Node head;
            private Node tail;

            public LinkedList(int ...values) {
                head = tail = null;

                for (int i : values) {
                    if (head == null) {
                        head = new Node(i, null);
                        tail = head;
                        continue;
                    }

                    tail.next = new Node(i, null);
                    tail = tail.next;
                }
            }

            public boolean isBroken() {
                Node foundTail = getTail();
                return foundTail != tail;
            }

            private Node getTail() {
                if (head == null) {
                    return null;
                }

                Node n = head;

                while (n.next != null) {
                    n = n.next;
                }

                return n;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof LinkedList other) {
                    return this.equals(other);
                }

                return false;
            }

            private boolean equals(LinkedList other) {
                Node myCurrent = head;
                Node otherCurrent = other.head;

                while (myCurrent != null && otherCurrent != null) {
                    if (myCurrent.value != otherCurrent.value) {
                        return false;
                    }

                    myCurrent = myCurrent.next;
                    otherCurrent = otherCurrent.next;
                }

                return myCurrent == null && otherCurrent == null;
            }
        }

        @Test
        public void test() throws IllegalAccessException {
            LinkedList original = new LinkedList(1, 2, 3, 4, 5);
            LinkedList clone = cloner.deepClone(original);

            assertEqualButNotSame(original, clone);

            assertFalse(original.isBroken());
            assertFalse(clone.isBroken());
        }
    }

    static class Element {
        private double value;

        public Element(double value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Element e) {
                return value == e.value;
            }

            return false;
        }
    }

    @Nested
    class ClonesArrayElements {
        @Test
        public void test_1dArray() throws IllegalAccessException {
            Element[] array = { new Element(1), new Element(2), new Element(3) };
            Element[] clone = cloner.deepClone(array);

            assertArrayElementsEqualButNotSame(array, clone, 1);
        }

        @Test
        public void test_2dArray() throws IllegalAccessException {
            Element[][] array = {
                { new Element(1), new Element(2), new Element(3) },
                { new Element(4), new Element(5), new Element(6) },
                { new Element(7), new Element(8), new Element(9) }
            };

            Element[][] clone = cloner.deepClone(array);

            assertArrayElementsEqualButNotSame(array, clone, 2);
        }

        @Test
        public void test_5dArray() throws IllegalAccessException {
            var array = make5dArray();
            var clone = cloner.deepClone(array);

            assertArrayElementsEqualButNotSame(array, clone, 5);
        }

        private Element[][][][][] make5dArray() {
            final int length = 5;

            Element[][][][][] array = new Element[length][][][][];

            int counter = 0;

            for (int a = 0; a < length; ++a) {
                array[a] = new Element[length][][][];

                for (int b = 0; b < length; ++b) {
                    array[a][b] = new Element[length][][];

                    for (int c = 0; c < length; ++c) {
                        array[a][b][c] = new Element[length][];

                        for (int d = 0; d < length; ++d) {
                            array[a][b][c][d] = new Element[length];

                            for (int e = 0; e < length; ++e) {
                                array[a][b][c][d][e] = new Element(counter);
                                ++counter;
                            }
                        }
                    }
                }
            }

            return array;
        }

        @Test
        public void test_jaggedArray() throws IllegalAccessException {
            Element[][] jagged = makeJaggedArray();
            Element[][] clone = cloner.deepClone(jagged);

            assertArrayElementsEqualButNotSame(jagged, clone, 2);
        }

        private Element[][] makeJaggedArray() {
            int[] sizes = { 1, 2, 0, 5, 6, 10 };
            Element[][] array = new Element[sizes.length][];

            int counter = 0;

            for (int i = 0; i < array.length; ++i) {
                array[i] = new Element[sizes[i]];

                for (int j = 0; j < array[i].length; ++j) {
                    array[i][j] = new Element(counter);
                    ++counter;
                }
            }

            return array;
        }
    }

    @Nested
    class ClonesListElements {
        @Test
        public void test_ArrayList() throws IllegalAccessException {
            testList(new ArrayList<>());
        }

        @Test
        public void test_LinkedList() throws IllegalAccessException {
            testList(new LinkedList<>());
        }

        private void testList(List<Element> list)
            throws IllegalAccessException
        {
            for (int i = 0; i < 10; ++i) {
                list.add(new Element(i));
            }

            List<Element> clone = cloner.deepClone(list);

            assertInstanceOf(list.getClass(), clone);
            assertIterableElementsEqualButNotSame(list, clone);
        }
    }
}
