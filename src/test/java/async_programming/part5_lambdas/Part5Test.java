package async_programming.part5_lambdas;

import org.junit.jupiter.api.Test;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Part5Test {

    @FunctionalInterface //not mandatory
    interface MyFunIf {
        boolean myMethod(Integer input); //must have one abstrct method

        static int my_static_method() {
            return 1;
        }

        default int defaultMethod() {
            return 2;
        }
    }

    interface MyGemericFunIf<T> {
        boolean myMethod(T input);

        //this is another T....
        static <T> Function<T, T> staticIdentityAndPrinter() {
            return t -> {
                System.out.println("*** converter: " + t + " is:" + t.getClass().getSimpleName());
                return t;
            };
        }

        //this is MyGemericFunIf T
        default Function<T, T> identityAndPrinter() {
            return t -> {
                System.out.println("*** converter: " + t);
                return t;
            };
        }
    }

    class SomeThing {
        private final int id;

        SomeThing(int id) {
            this.id = id;
        }

        String plus(SomeThing other) {
            return id + "::" + other.id;
        }
    }

    class AcceptsBiFunction<T> {
        private final T left, right;

        AcceptsBiFunction(T left, T right) {
            this.left = left;
            this.right = right;
        }

        public <U> U doSomething(BiFunction<T, T, U> bi) {
            return bi.apply(left, right);
        }
    }


    @Test
    void test_lambda_types() throws InterruptedException {

        //lambda is implementation of FunctionalInterface interface...
        MyFunIf lamb = (Integer input) -> {
            return input > 0;
        };

        MyGemericFunIf<String> otherLamb = String::isBlank;

        var staticIdentityAndPrinter = MyGemericFunIf.staticIdentityAndPrinter();
        //Function of <Object,Object)
        List.of("aa", "bb", "cc").stream().map(staticIdentityAndPrinter).forEach(_ -> {
        });
        List.of("aa", 1, "cc").stream().map(staticIdentityAndPrinter).forEach(_ -> {
        });

        new MyGemericFunIf<String>() {
            @Override
            public boolean myMethod(String input) {
                return false;
            }
        }.identityAndPrinter();

        SomeThing someThing = new SomeThing(1);
        SomeThing otherSomeThing = new SomeThing(2);
        var acceptsBiFun = new AcceptsBiFunction<SomeThing>(someThing, otherSomeThing);
        System.out.println("acceptsBiFun = " + acceptsBiFun.doSomething(SomeThing::plus));
    }

    static int j = 0;

    @Test
    void test_lambda_as_local_class() throws InterruptedException {
        var i = 0;
        var wrapped_i = new Object() {
            int i = 0;
        };

        Optional.of(1).ifPresent(
                (value) -> {
                    //i = value; cannot do that, must be effectively final
                    j = value; //static are ok to change
                    wrapped_i.i = value;  //works as well!!! wrapper is final!!!
                }
        );
    }


    @Test
    void test_lambda_composition_predicate() throws InterruptedException {

        Predicate<Integer> largeEnough = (Integer test) -> test>100;
        Predicate<Integer> odd = (Integer test) -> test % 2 == 0;
        Predicate<Integer> notTooLarge = new Predicate<Integer>() {
            @Override
            public boolean test(Integer o) {
                Objects.requireNonNull(o);
                return o<1000;
            }
        };

        var acceptable = List.of(1,2,100,101,102,1001);

        var res = acceptable.stream().filter(largeEnough.and(notTooLarge.or(odd))).toList();
        System.out.println("accept:"+res);

        interface MyPredicate<T> {
            public boolean tester(T o);

            default MyPredicate<T> customOr(MyPredicate<T> other) {
                return (in) -> this.tester(in) || other.tester(in);
            }
        }

        MyPredicate<Integer> isTwoCustom = (Integer test) -> test == 2;
        MyPredicate<Integer> isThreeCustom = (Integer test) -> test == 3;

        acceptable.stream().filter(Objects::nonNull).filter((i) -> isTwoCustom.customOr(isThreeCustom).tester(i));
        //Objects::nonNull is not a predicatem and is missing implementation for and/or/not

        //acceptable.filter(((i)-> i==1).and(>>>)) does not see and / or   cannot chain... do

        acceptable.stream().filter(((Predicate<Integer>)((i)-> i==1)).and(isTwoCustom::tester)).forEach(System.out::println); //casted to Pred,,,

        assertThrows(
                ClassCastException.class,
                () -> {
                    Predicate<Integer> normalPredicate = (Predicate<Integer>)isTwoCustom;
                    //does not implement Predicates and, or...
                    acceptable.stream().filter(normalPredicate.and(odd)).forEach(System.out::println);
                }
        );

    }

    @Test
    void test_lambda_composition_chain_functions() throws InterruptedException {

        Function<Integer,Integer> plus2 = in -> in + 2;
        Function<Integer,Integer> times3 = in -> in * 3;

        var in = List.of(1,2,3);

        in.stream().map(plus2.andThen(times3)).forEach(System.out::println);  // (x + 2) * 3
        in.stream().map(plus2.compose(times3)).forEach(System.out::println);  // (x * 3) + 2
    }

    @Test
    void test_lambda_consumers() {

        var list = List.of(1,2,3);
        //consumer
        Consumer<Integer> consumer = in -> System.out.println(in);
        list.forEach(i -> System.out.println(i));
        Consumer<Integer> consumer2 = in -> System.out.println(in.hashCode());
        list.forEach(consumer.andThen(consumer2));

        //do multiple consumers at once

        var map = Map.of(1,"i1",2,"i2");
        //biconsumer
        BiConsumer<Integer, String> biConsumer = (k,v) -> System.out.println("k:"+k+",v:"+v);
        map.forEach((k,v)-> System.out.println(k+":"+v));

    }

}
