package advanced_features.part6_lambdas.part5_generics;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Part6Test {

    @Test
    void test_lambdas() {

        var names = new ArrayList<String>(List.of( "Bb", "Aaaa", "Cccc"));

        names.sort(Comparator.naturalOrder());
        System.out.println(names);

        names.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.compare(o1.length(), o2.length());
            }
        });
        System.out.println(names);

        names.sort((first, second) -> Integer.compare(first.length(), second.length()));
        System.out.println(names);

        names.stream().forEach(name -> System.out.println(name)); //single parameter, skip oarentheses

        names.stream().map(String::toUpperCase).forEach(System.out::println);
    }

    //forces one abstract function check in this interface!!!
    @FunctionalInterface
    interface MyFunIn {
        int myFun(int a, int b);
        default int myIFun(int a, int b, int c) {return 1;}
    }

    static void test(MyFunIn myFunIn, int a, int b, int expected) {
        assert expected == myFunIn.myFun(a,b);
    }

    @Test
    void test_my_lambd() {
        System.out.println("---testing 2+3=5");
        test((a,b)-> a + b, 2, 3, 5);

        System.out.println("---testing 2+3=6 error");
        assertThrows(AssertionError.class,
                () -> test((a,b)-> a + b, 2, 3, 6)
        );
    }

    record Product(String name, int price) {}

    class SomeClass {
        public static final boolean myStaticMethod_isExpensive(Product product) {
            return product.price >= 10;
        }
    }

    @Test
    void test_method_refs() {
        Product product0 = new Product("rubbish item", 0);
        Product product1 = new Product("expensive item", 10);
        Product product2 = new Product("luxury item", 100);

        var products = List.of(product0, product1, product2);

        //calling objects out method
        var outInstance = System.out;
        products.forEach(outInstance::println);

        //method in own class
        products.stream().forEach(this::increment);
        System.out.println("local counter" + ctr.get());

        //method in product instance
        var names = products.stream().map(Product::name).toList();
        System.out.println("prod names list:" + names);

        var namesSet = products.stream().map(Product::name).collect(Collectors.toCollection(HashSet::new));
        System.out.println("prod set:" + namesSet);


        //constructor and binary accumulator
        var total_price = products.stream()
                .map(Product::price)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("prod total_price:" + total_price);

        //calling static SomeClass method
        var expensive = products.stream().filter(SomeClass::myStaticMethod_isExpensive).toList();
        System.out.println("simply expensive:" + expensive);

    }

    AtomicInteger ctr = new AtomicInteger();
    private void increment( Product product) {
        ctr.incrementAndGet();
    }
}
