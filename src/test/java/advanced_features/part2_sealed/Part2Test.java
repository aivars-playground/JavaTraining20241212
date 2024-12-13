package advanced_features.part2_sealed;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class Part2Test {

    interface Animal {}
    sealed interface Pet extends Animal permits Dog, Cat, Fish {}   //permits is not mandatory for same package

    //class Racoon extends Pet {} not permitted

    final class Dog implements Pet {}

    sealed interface Cat extends Pet {} //implementations myst be in same package
    final class Racoon implements Cat {}
    final class Siamese implements Cat {}

    non-sealed interface Fish extends Pet {}  //breaking seal, can be implemented in another package
    class GoldFish implements Fish {}

    //--------------------------------------------------
    //--------------------------------------------------
    //--------------------------------------------------

    record Order(List<OrderLine> lines) {}
    sealed interface OrderLine permits SaleOrderLine, DiscountOrderLine {}
    record SaleOrderLine(String product, int quantity, float price) implements OrderLine {}
    record DiscountOrderLine(String code, float price) implements OrderLine {}

    public float calculatePrice(Order order) {
        float total = 0f;

        for (OrderLine line : order.lines) {
            if (line instanceof SaleOrderLine saleOrderLine) {
                total += saleOrderLine.price;
            } else if (line instanceof DiscountOrderLine discountOrderLine) {
                total -= discountOrderLine.price;
            }
        }

        return total;
    }

    @Test
    public void test_sealed() {

        var sale1 = new SaleOrderLine("ab1", 1, 1f);
        var sale2 = new SaleOrderLine("ab2", 2, 2f);
        var disc1 = new DiscountOrderLine("BF2024",0.5f);

        var order = new Order(List.of(sale1, sale2, disc1));
        assertEquals(2.5f, calculatePrice(order));
    }

}


