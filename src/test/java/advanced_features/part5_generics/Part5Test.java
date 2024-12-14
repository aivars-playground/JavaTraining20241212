package advanced_features.part5_generics;

import advanced_features.part3_pattern.Part3Test;
import advanced_features.part5_generics.basictree.InnerNode;
import advanced_features.part5_generics.basictree.LeafNode;
import advanced_features.part5_generics.sortedniode.MaxValLeafNode;
import advanced_features.part5_generics.sortedniode.MaxValueInnerNode;
import com.sun.source.tree.NewArrayTree;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

public class Part5Test {

    @Test
    void test_generic_class() {

        var three = new LeafNode<Integer>(3);         //instanciating type parameter and creatng instance
        var five = new LeafNode<>(5); //type inference

        var six = new LeafNode<>(6);

        var tree = new InnerNode<>(new LeafNode<>(2),new InnerNode<Integer>(three,five));
        System.out.println(tree);
    }

    @Test
    void test_generic_method() {
        ///Pair<> p1a = new Pair("aaa","vvv"); left hand side needs parameter
        Pair<String,String> example = new Pair("aaa","vvv");

        var p1 = new Pair<String,Integer> ("a",1);
        var p2 = Pair.<Integer,String>of(2,"B");

        var p1New = p1.withOtherRight(LocalDate.now());
        System.out.println("---p1:"+p1 + " --> p1New:" +p1New);

        System.out.println("---p1:"+p1 + " reversed:" + p1.reverse());

        var p2mapped = p2.map((i,s) -> new Pair(i.describeConstable(), s.describeConstable()));
        System.out.println("---p2:"+p2 + " transformed to:" + p2mapped);

    }

    @Test
    void test_generic_class_parameter_bound() {

        var two = new MaxValLeafNode<>(2);
        var three = new MaxValLeafNode<Integer>(3);         //instanciating type parameter and creatng instance
        var five = new MaxValLeafNode<>(5); //type inference

        var tree = new MaxValueInnerNode<>(two,new MaxValueInnerNode<Integer>(three,five));

        System.out.println(tree.getValue());
    }


    public interface HasId { int id(); }
    public interface HasName { String name(); }
    public record Person(int id, String name, String address) implements HasId, HasName {}

    static <T extends HasId & HasName >List<String> sortByIdAndGetName(List<T> list) {
        return list.stream().sorted(Comparator.comparing(T::id)).map(T::name).toList();
    }

    @Test
    void test_generic_class_parameter_bound_another() {
        Person pJohn = new Person(11,"John","Doe");
        Person pJane = new Person(22,"Jane","Doe");
        System.out.println(List.of(pJane,pJohn));
    }

    interface Animal {}
    record Dog(String name) implements Animal {}
    record Cat(String name) implements Animal {}

    @Test
    void test_generic_class_inheritance() {
        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("Bob"));
        dogs.add(new Dog("John"));

        //List<Animal> animals = dogs; incompatible type
        //generics are
        //   ***** invariant...   S extends T ---> does not mean X(S) extends X(T)
        //      ** what is covariant - what we expeced  that List[Dog] extends List[Animal]
        //              ,,,why...     List of dog -> List of Animal... add animal Cat???

    }

    void acceptUnbounded(List<?> params){};
    void acceptUpperBound(List<? extends Animal> params){}; //parent class is top of the hierarchy!!!!
    void acceptUpperBoundCat(List<? extends Cat> params){};
    void acceptLowerBoundDog(List<? super Dog> params){};

    @Test
    void test_generic_class_wildcard() {

        var cat = new Cat("bob");
        var dog = new Dog("pooch");

        List<Animal> animalsList = List.of(cat,dog);
        List<Cat> cats = List.of(cat);

        List<?> unbounded                    = animalsList;
        List<? extends Animal> upperBound1   = animalsList;
        List<? extends Animal> upperBound2   = cats;
        List<? extends Cat>    upperBound3   = cats;
        List<? super Dog>      lowerBound1   = animalsList;   //animal list matches caoture,, it is suoer of Dog,, !!!!!!
        //List<? super Dog>      lowerBound2   = cats;


        List<? super Dog>      lowerBoundDogs = new ArrayList<>(1);
        lowerBoundDogs.add(dog);
        Animal catAnimal2 = cat;
        //lowerBoundDogs.add(catAnimal2);

        List<? extends Cat> upperBoundCatAn = new ArrayList<>();
        //upperBoundCatAn.add(cat);
        //upperBoundCatAn.add(catAnimal);
        //Cat is not a capture!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //Capture is unknown type... not Cat, not Animal!!!!

        //static <T> void copy(List<? super T> dest, List<? extends T> dest)
        List<Cat> catsSource = List.of(cat,cat);
        List<Animal> animalsListDestination = new ArrayList<>();

        List catRaw = List.of(cat);
        List<Cat>    catFromRawCat = catRaw;
        List<Animal> aniFromRawCat = catRaw;
        List<Dog>    dogFromRawCat = catRaw;

        System.out.println(" c:"+catFromRawCat + " a:"+aniFromRawCat +" d:"+dogFromRawCat);

        //Heap polution... happens when using raw types
        assertThrows(ClassCastException.class,
                () -> {
                    for (Dog d : dogFromRawCat) {
                        System.out.println(d instanceof Dog);
                    }
        });
    }


    class ParamClass<T> {
        void classDecides(T param) {
            doSomething(param);
            switchThis(param);
        }

        //does not work!!!!
        private void doSomething(String param) {
            System.out.println("---string parameter:"+param);
        }
        private void doSomething(T param) {
            System.out.println("---arbitrary parameter:"+param);
        }

        private void switchThis(T param) {
            switch (param) {
                case Integer in -> System.out.println("Int"+in);
                case String  st -> System.out.println("String"+st);
                default -> System.out.println("Default");
            }
        }

    }

    @Test
    void test_generic_class_findType() {
        new ParamClass<Integer>().classDecides(1);
        new ParamClass<String>().classDecides("abc");
    }

    @Test
    void test_generic_array_covariant() {
        Dog[] dogs = new Dog[3];
        dogs[0] = new Dog("Bob");
        dogs[1] = new Dog("John");

        System.out.println(dogs instanceof Animal[]);
        Animal[] animals = dogs;
        System.out.println(animals instanceof Dog[]);
        assertThrows(ArrayStoreException.class,
                () -> {animals[2] = new Cat("Meow");}
        );

        Animal[] animalsAll = new Animal[3];
        animalsAll[0] = new Dog("Bob");
        animalsAll[1] = new Dog("John");
        animalsAll[2] = new Cat("Meow");
    }

}
