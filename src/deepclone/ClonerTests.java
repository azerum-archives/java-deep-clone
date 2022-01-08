package deepclone;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class ClonerTests {
    @Nested
    class ImmutableTypes {
        @Test
        public void test_primitiveTypes() throws IllegalAccessException {
            int a = 42;
            char u = 'u';

            int aClone = Cloner.deepClone(a);
            int uClone = Cloner.deepClone(u);

            assertEquals(a, aClone);
            assertEquals(u, uClone);
        }

        @Test
        public void test_wrappersOfPrimitiveTypes() throws IllegalAccessException {
            Integer a = 42;
            Character u = 'u';

            Integer aClone = Cloner.deepClone(a);
            Character uClone = Cloner.deepClone(u);

            assertSame(a, aClone);
            assertSame(u, uClone);
        }

        @Test
        public void test_string() throws IllegalAccessException {
            String s = "Hello, world";
            String sClone = Cloner.deepClone(s);

            assertSame(s, sClone);
        }

        @Test
        public void test_record() throws IllegalAccessException {
            record SomeRecord(int foo, String bar) {}

            SomeRecord r = new SomeRecord(42, "Lorem");
            SomeRecord rClone = Cloner.deepClone(r);

            assertSame(r, rClone);
        }
    }

    @Test
    public void test_null() throws IllegalAccessException {
        Object original = null;
        Object clone = Cloner.deepClone(original);

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
            C clone = Cloner.deepClone(original);

            assertEquals(getter.apply(original), getter.apply(clone));

            setter.accept(clone, newValue);

            assertNotEquals(getter.apply(original), getter.apply(clone));
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

            Person clone = Cloner.deepClone(bob);

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
    class NestedObjectWithTwoSameReferences {
        static class Foo {
            //По внутренеей логике класса Foo,
            //bar1 и bar2 всегда должны ссылатся на один и тот же объект
            private Bar bar1;
            private Bar bar2;

            public Foo() {
                bar1 = new Bar();
                bar2 = bar1;
            }

            //Если bar1 != bar2, то логика объекта сломана
            public boolean isBroken() {
                return bar1 != bar2;
            }
        }

        static class Bar { }

        @Test
        public void test() throws IllegalAccessException {
            Foo foo = new Foo();
            Foo clone = Cloner.deepClone(foo);

            assertFalse(foo.isBroken());
            assertFalse(clone.isBroken());
        }
    }

    @Test
    public void test_linkedList() throws IllegalAccessException {
        LinkedList<Integer> numbers = new LinkedList<>();

        for (int i = 0; i < 10; ++i) {
            numbers.add(i);
        }

        LinkedList<Integer> clone = Cloner.deepClone(numbers);

        assertEquals(numbers, clone);
        assertNotSame(numbers, clone);

        clone.addLast(42);

        int last = 0;

        for (int i : clone) {
            last = i;
        }

        assertEquals(last, 42);

        assertNotEquals(numbers, clone);
    }
}
