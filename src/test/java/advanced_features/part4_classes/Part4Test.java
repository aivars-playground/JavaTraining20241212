package advanced_features.part4_classes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class EnclosingWithStaticClass {

    private static int counter = 0;
    private static LocalDate date = LocalDate.of(2000, 1, 1);

    private String name = "John Doe";

    private static void printCounter() {
        System.out.println(counter);
        //System.out.println(name); cannot access non-static
    }

    public void printCounterAndName() {
        System.out.println(counter);
        System.out.println(name);
    }

    private static void printDate() {
        System.out.println(date);

    }

    static class StaticNestedClass {
        //overwrites!!
        private static LocalDate date = LocalDate.of(2100, 1, 1);
        public static void printDate() {
            System.out.println("internal:"+date);
            System.out.println("external:"+EnclosingWithStaticClass.date);
        }

        private String name = "inner";


        public void print() {
            //can access static (including private) outer class members
            System.out.println("counter:" + counter + " name: " + name);
            printCounter();
            printDate();
        }
    }
}

class EnclosingWithInnerClass {
    private static LocalDate date = LocalDate.of(2000, 1, 1);

    private int counter = 0;

    public void createInnerClassAndAccessSelf() {
        var inner = this.new InnerClass();
        inner.secretPrint();
        StaticNestedClass.accessSecret(this);
    }

    private void secretPrint() {
        System.out.println("outer:secret");
    }

    class InnerClass {
        private static LocalDate date = LocalDate.of(2100, 1, 1);
        private int counter = 100;
        public void printData() {
            System.out.println("internal static:"+date);
            System.out.println("external static:" + EnclosingWithInnerClass.date);

            System.out.println("internal counter:"+counter);
            System.out.println("external counter:" + EnclosingWithInnerClass.this.counter);
        }

        private void secretPrint() {
            System.out.println("inner:secret");
            EnclosingWithInnerClass.this.secretPrint();
        }
    }

    static class StaticNestedClass {
        public static void accessSecret(EnclosingWithInnerClass enclosingWithInnerClass) {
            System.out.println("accessing secret by outer reference");
            enclosingWithInnerClass.secretPrint();
        }
    }
}

class EnclosingWithRecord {

    static LocalDate date = LocalDate.of(2000, 1, 1);
    int counter = 0;

    //record acts as static nested
    record MyRecord() {
        void doSomething() {
            System.out.println(date);
            //System.out.println(EnclosingWithRecord.this.counter);
        }
    }
}


class WithLocalType {

    static void callWithLocalType() {

        //counter must be "effectively final"
        int counter = 0;
        int counter2 = 0;

        class Counter {
            int internalCounter = 0;
            void inc() {
                internalCounter++;
            }
        }
        var anotherCounter = new Counter(); //anotherCounter is effectively final ref
        anotherCounter.inc();


        //counter++;   //see note above
        class LocalType {
            int counter2 = 333;
            void doSomethingOnLocal() {
                //counter++;    //see note above
                counter2++;     //overriten
                System.out.println(counter);
                anotherCounter.inc();
            }
        }

        //counter++;
        anotherCounter.inc();

        var obj = new LocalType();
        obj.doSomethingOnLocal();

        System.out.println("internal counter"+anotherCounter.internalCounter);
    }

}


record MyRecord(String name) {

    private static int statiCounter = 0;

    //instance variables not allowed
    //private int instanceCounter;

    public class InnerClass {
        public void doSomething() {
            System.out.println("access record:"+(statiCounter++)+name);
        }
    }
}

public class Part4Test {

    @Test
    void test() {

        System.out.println("-------------------nested");
        EnclosingWithStaticClass.StaticNestedClass.printDate();

        var nestedClass= new EnclosingWithStaticClass.StaticNestedClass();
        nestedClass.print();

        System.out.println("-------------------inner");

        var innerClass= new EnclosingWithInnerClass().new InnerClass();
        innerClass.printData();

        new EnclosingWithInnerClass().createInnerClassAndAccessSelf();

        new EnclosingWithRecord.MyRecord();

        var recordInner = new MyRecord("aa").new InnerClass();
        recordInner.doSomething();
        recordInner.doSomething();
        new MyRecord("bbb").new InnerClass().doSomething();

        System.out.println("-------------------local");
        WithLocalType.callWithLocalType();

        System.out.println("-------------------anonymous");

        var rec1 = new MyRecord("vvv");
        var rec2 = new MyRecord("aaaa");
        var list = new ArrayList<MyRecord>();
        list.add(rec1);
        list.add(rec2);

        System.out.println(list);

        var prefix = "a";

        //prefix = "b";  the same as local type, anonymous can access only effectively final

        var res = list.stream().filter(
                (rec) -> rec.name().startsWith(prefix)
        ).toList();

        System.out.println(res);


    }
}
