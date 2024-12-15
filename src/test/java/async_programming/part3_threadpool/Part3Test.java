package async_programming.part3_threadpool;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;

public class Part3Test {


    record Quotation(String name, Integer price) {}
    static Supplier<Quotation> prepare_supplier(String name, Integer price) {
        return () -> {
            var sleepTime = new Random().nextInt(100);
            try {Thread.sleep(sleepTime);} catch (InterruptedException e) { e.printStackTrace(); throw new RuntimeException(e);}
            System.out.println("create on thread:"+Thread.currentThread().getName());
            return new Quotation(name, price);
        };
    }

    @Test
    void test_executors() throws InterruptedException {

        Function<Quotation, Quotation> convert = (Quotation quotation) -> {
            System.out.println("convert on thread:"+Thread.currentThread().getName());
            return quotation;
        };


        //using default executor
        CompletableFuture<Quotation> quoteFuture = CompletableFuture.supplyAsync(prepare_supplier("Quotation", 100));

        //on the same executor!!!!
        var onDefault = quoteFuture.thenApply(convert);


        //async moves to another
        Executor customExecutor1 = Executors.newFixedThreadPool(3);
        var onCustom1 = onDefault.thenApplyAsync(convert, customExecutor1);

        //async again moves to another
        Executor customExecutor2 = Executors.newVirtualThreadPerTaskExecutor();
        var onCustom2 = onCustom1.thenApplyAsync(convert, customExecutor2);

        //async Executor back to default
        var onCustom3 = onCustom2.thenApplyAsync(convert, ForkJoinPool.commonPool());

        //on Swing single thread!!!
        var onSwing = onCustom3.thenApplyAsync(convert, SwingUtilities::invokeLater);  //AWT-EventQueue-0

        onSwing.join();

    }

}
