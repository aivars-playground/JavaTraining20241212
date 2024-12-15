package advanced_features.part8_optionals;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Part8Test {

    @Test
    void test_optionals() {
        //optionals are nor serializable
        //optionals are value, not identity classes, maent to be interchangable


        System.out.println("id:1 " + ProductRepo.findById(1));
        System.out.println("id:9 " + ProductRepo.findById(9));

        var element11 = ProductRepo.findById(11);
        if (element11.isPresent()) {
            System.out.println("id:11 " + element11.get());
        } else {
            System.out.println("id:11 missing");
        }

        var element12 = ProductRepo.findById(12).orElse(null);
        System.out.println("id:12 " + element12);

        var element13 = ProductRepo.findById(13).orElseGet(() -> new Product(13,"fake product") );
        System.out.println("id:13 " + element13);


        assertThrows(NoSuchElementException.class, () -> ProductRepo.findById(14).get());


        System.out.println(ProductRepo.findById(1).map(Product::name).orElse("notfound"));

        Set<Integer> allIdsAvailable = Set.of(1,2,3,4,5,6,7,8,9,10);
        Set<Integer> discountedIds = Set.of(2,10);

        var discountedAvailable = allIdsAvailable.stream().filter(id -> discountedIds.contains(id)).toList();
        System.out.println("available at discount" + discountedAvailable);

        LongStream ls = LongStream.range(1,10).flatMap(i -> LongStream.range(0,i));
        System.out.println(Arrays.toString(ls.toArray()));

    }
}

record Product(int id, String name) {}

class ProductRepo {

    private static final List<Product> products =
            List.of(
                    new Product(1, "aaa"),
                    new Product(2, "vvv")
            );

    static Optional<Product> findById(int id) {
        return products.stream().filter(p -> p.id() == id).findFirst();
    }
}