package advanced_features.part1_records;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class Part1Test {

    /**
     * Careful when overrifing record generated methods
     */
    @Test
    public void test_record_override_issue() {

        record Person(String firstName, String middleName, String lastName) {

            @Override
            public String middleName() {
                //not good idea
                return middleName != null && !middleName.isBlank() ? middleName : "[unknown]";
            }
        }

        var person = new Person("first","","last");
        System.out.println(person);                 //Person[firstName=first, middleName=, lastName=last]
        System.out.println(person.middleName());    //[unknown]

        var clone = new Person(person.firstName(),person.middleName(),person.lastName());

        System.out.println(person.equals(clone));   //false
        //equals is not using overriden field accessor
    }

    /**
     *  valid scenario - custom equals -> hashcode
     */
    @Test
    public void test_record_valid_override() {

        record Person(Long uniqueId, String name) {

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Person other && this.uniqueId == other.uniqueId;
            }

            @Override
            public int hashCode() {
                return Long.hashCode(uniqueId);
            }
        }

        var person = new Person(1L, "originalName");
        var samePerson = new Person(1L, "changedName");

        assertEquals(samePerson, person);
    }

    /**
     *  custom cannonical constructor
     *
     *  enforce immutable collections
     *  validate input
     */
    @Test
    public void test_record_cannonical_constructor() {

        record Product(long id, String name) {}
        record OrderItem(Product product, Long quantity, BigDecimal price) {}
        record Order(long id, String customer, List<OrderItem> items) {
            //cannonical constructor - override to modify before assignment
            public Order(long id, String customer, List<OrderItem> items) {
                assert (customer != null);
                this.id = id;
                this.customer = customer;
                this.items = List.copyOf(items); //create immutable copy
            }
        }

        Product product = new Product(1L, "product");
        OrderItem orderItem = new OrderItem(product, 1L, BigDecimal.valueOf(2));
        Order order = new Order(1L, "customer", List.of(orderItem));

        OrderItem anotherOrderItem = new OrderItem(product, 2L, BigDecimal.valueOf(3));
        //list is immutable, should fail
        assertThrows(UnsupportedOperationException.class,() -> order.items.add(anotherOrderItem));
    }

    /**
     *  custom compact constructor
     */
    @Test
    public void test_record_compact_constructor() {

        record Product(long id, String name) {}
        record OrderItem(Product product, Long quantity, BigDecimal price) {}
        record Order(long id, String customer, List<OrderItem> items) {
            //compact constructor - override to modify after !!! assignment
            public Order {
                assert (customer != null);
                items = List.copyOf(items);
            }
        }

        Product product = new Product(1L, "product");
        OrderItem orderItem = new OrderItem(product, 1L, BigDecimal.valueOf(2));

        //validate order
        assertThrows(AssertionError.class, () -> new Order(1L, null, List.of(orderItem)));

        Order order = new Order(1L, "customer", List.of(orderItem));

        OrderItem anotherOrderItem = new OrderItem(product, 2L, BigDecimal.valueOf(3));
        //list is immutable, should fail
        assertThrows(UnsupportedOperationException.class,() -> order.items.add(anotherOrderItem));
    }

    /**
     *  order of constructors
     */
    @Test
    public void test_record_constructor_order() {

        record Product(Long id, String name, String description) {
            public Product{}
            //public Product(Long id, String name, String description) alredy defined 3 arg constructor
            public Product(Long id, String name) {
                this(id,name,null);
            }
            public static Product createOrderWithRandomId(String name) {
                return new Product( new Random().nextLong(),name);
            }
        }

        Product nameOnly = new Product(1L, "abc");
        Product nameAndDescription = new Product(2L, "def", "descr");
        Product withStaticFactory = Product.createOrderWithRandomId("random order");
    }


    /**
     *  interfaces
     */
    @Test
    public void test_record_interface() {

        record Product(Long id, String name) implements Comparable<Product>{
            @Override
            public int compareTo(Product o) {
                return this.name.compareTo(o.name);
            }
        }

        var pA = new Product(111L, "pA");
        var pB = new Product(11L, "pB");
        var pC = new Product(1L, "pC");

        var products = new TreeSet<Product>();
        products.add(pC);
        products.add(pB);
        products.add(pA);

        assertEquals(products.stream().toList(), List.of(pA, pB, pC));

    }


    @Test
    public void test_record_builder() {
        record Product(Long id, String name, String description) {

            public Builder copyBuilder() {
                return new Builder(this);
            }

            public static Builder builder() {
                return new Builder();
            }

            private static class Builder {
                private Long id;
                private String name;
                private String description;
                private Builder() {}
                private Builder(Product product) {
                    this.id = product.id;
                    this.name = product.name;
                    this.description = product.description;
                }
                public Builder id(Long id) {
                    this.id = id;
                    return this;
                }
                public Builder name(String name) {
                    this.name = name;
                    return this;
                }
                public Builder description(String description) {
                    this.description = description;
                    return this;
                }
                public Product build() {
                    return new Product(id, name, description);
                }
            }
        }

        var product1 = Product.builder()
                .id(1L)
                .name("product1")
                .description("product1")
                .build();

        var product2 = product1.copyBuilder()
                .description("something else")
                .build();

        assertNotEquals(product1, product2);
        System.out.println("p1:"+product1);
        System.out.println("p2:"+product2);
    }


}
