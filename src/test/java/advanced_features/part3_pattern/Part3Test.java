package advanced_features.part3_pattern;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Part3Test {

    record Order(List<OrderLine> lines) {}
    sealed interface OrderLine permits SaleOrderLine, DiscountOrderLine {}
    record SaleOrderLine(String product, int quantity, float price) implements OrderLine {}
    record DiscountOrderLine(String code, float price) implements OrderLine {}

    @Test
    public void test_instanceof_patern() {

        OrderLine ol = new SaleOrderLine("abc",1,1);

        if (ol instanceof SaleOrderLine sol) {
            System.out.println("price:" + sol.price);
        } else if (ol instanceof DiscountOrderLine dol) {
            System.out.println("price:" + dol.price);
        }

        boolean isSol = ol instanceof SaleOrderLine sol;
        System.out.println("isSol:" + isSol);

//        while (ol instanceof SaleOrderLine sol) {
//            System.out.println("price:" + sol.price);
//        }
    }

    @Test
    public void test_switch_patern() {
        var sale1 = new SaleOrderLine("ab1", 1, 1f);
        var sale2 = new SaleOrderLine("ab2", 2, 2f);
        var disc1 = new DiscountOrderLine("BF2024",0.5f);
        var order = new Order(List.of(sale1, sale2, disc1));

        var totalPrice = 0f;
        for (OrderLine line: order.lines) {
            switch (line) {
                case DiscountOrderLine disc -> totalPrice -= disc.price;
                case SaleOrderLine sale -> totalPrice += sale.price;
            }
        }
        assertEquals(2.5f, totalPrice);
    }


    @Test
    public void test_switch_patern_alternative() {
        var sale1 = new SaleOrderLine("ab1", 1, 1f);
        var sale2 = new SaleOrderLine("ab2", 2, 2f);
        var disc1 = new DiscountOrderLine("BF2024",0.5f);
        var order = new Order(List.of(sale1, sale2, disc1));

        var totalPrice = 0f;
        for (OrderLine line: order.lines) {
            float netAmount = switch (line) {
                case DiscountOrderLine disc -> (-disc.price);
                case SaleOrderLine sale -> sale.price;
            };
            totalPrice += netAmount;
        }
        assertEquals(2.5f, totalPrice);
    }

    @Test
    public void test_switch_patern_guards() {
        var sale1 = new SaleOrderLine("ab1", 1, 1f);
        var sale2 = new SaleOrderLine("ab2", 2, 2f);
        var disc1 = new DiscountOrderLine("BF2024", 0.5f);
        var order = new Order(List.of(sale1, sale2, disc1));

        var textOutput = "ITEMS";
        for (OrderLine line: order.lines) {
            switch (line) {
                case DiscountOrderLine disc                         -> textOutput += "\n discount:" + disc.code;
                case SaleOrderLine sale when sale.quantity ==1      -> textOutput += "\n prod:" + sale.product;
                case SaleOrderLine sale                             -> textOutput += "\n prod:" + sale.product + " qt:"+sale.quantity;
            }
        }
        System.out.println(textOutput);
    }

    @Test
    public void test_switch_patern_extract() {

        var sale1 = new SaleOrderLine("ab1", 1, 1f);
        var sale2 = new SaleOrderLine("ab2", 2, 2f);
        var disc1 = new DiscountOrderLine("BF2024", 0.5f);
        var order = new Order(List.of(sale1, sale2, disc1));

        var textOutput = "ITEMS";
        for (OrderLine line: order.lines) {
            switch (line) {
                case DiscountOrderLine(var discountCode, var value) -> textOutput += "\n discount:" + discountCode;
                case SaleOrderLine(var prodName, var qt, var prc) when qt == 1 -> textOutput += "\n prod:" + prodName;
                case SaleOrderLine(var prodName, var qt, var prc) -> textOutput += "\n prod:" + prodName + " qt:"+qt;
            }
        }
        System.out.println(textOutput);
    }

    @Test
    public void test_switch_patern_extract_nested() {

        record Address(String line1, String line2, String country) {}
        record Customer(String firstName, String lastName) {}
        record Shipment(Customer customer, Address address, String code) {}

        var shipment = new Shipment(
                new Customer("name","last"),
                new Address("l1ne1", "line2", "UK"),
                "000000000001"
        );

        var nameCountry  = switch (shipment) {
            case Shipment(Customer(var firstName, var ignored), Address(var ignored1, var ignored2, var country), var ignored3) -> {
                yield  firstName +"@"+ country;}
        };
        assertEquals("name@UK", nameCountry);

        var nameCountryAgain =
                shipment.customer.firstName +"@"+ shipment.address.country;

        assertEquals("name@UK", nameCountryAgain);

    }

}
