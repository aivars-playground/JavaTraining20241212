package async_programming.part4_errorhandling;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Part4Test {

    record Quotation(String name, Integer price) {}
    static Supplier<Quotation> prepare_supplier(String name, Integer price) {
        return () -> {
            var sleepTime = new Random().nextInt(100);
            try {Thread.sleep(sleepTime);} catch (InterruptedException e) { e.printStackTrace(); throw new RuntimeException(e);}
            if (price == null) {
                System.out.println("*** supplier: failed:"+name);
                throw new IllegalArgumentException("Price cannot be null");
            }
            System.out.println("*** supplier: ok :"+name);
            return new Quotation(name, price);
        };
    }

    static <T> Function<T, T> identityAndPrinter() {
        return t -> {
                System.out.println("*** converter: ok");
                return t;
        };
    }

    @Test
    void test_errors() throws InterruptedException {

        System.out.println("=============no errors");
        CompletableFuture<Quotation> quoteFuture = CompletableFuture.supplyAsync(prepare_supplier("Quotation1", 100));
        quoteFuture.thenApply(identityAndPrinter()).thenAccept(System.out::println).join();

        System.out.println("=============supplier failed. whole pipeline failed");
        CompletableFuture<Quotation> quoteFutureFailed = CompletableFuture.supplyAsync(prepare_supplier("Quotation2", null));
        try {
            quoteFutureFailed.thenApply(identityAndPrinter()).thenAccept(System.out::println).join();
        } catch (Exception ex) {
            System.out.println("PIPELINE:" + ex.getMessage());
        }

        System.out.println("=============supplier failed. using default value");
        CompletableFuture<Quotation> quoteFutureDefault = CompletableFuture.supplyAsync(prepare_supplier("Quotation3", null));
        quoteFutureDefault
                .exceptionally( e-> {
                    System.out.println("*******exceptionally - using default value");
                    return new Quotation("default quotation due to" +e.getMessage(),11);
                }).thenApply(identityAndPrinter()).thenAccept(System.out::println).join();


        System.out.println("=============supplier failed. handling exception");
        CompletableFuture<Quotation> quoteFutureHandle = CompletableFuture.supplyAsync(prepare_supplier("Quotation4", null));
        quoteFutureHandle
                .handle((value,ex) -> {
                    if (ex == null) {
                        System.out.println("*******handle - OK");
                        return value;
                    } else {
                        System.out.println("*******handle - ex:"+ex.getMessage());
                        return new Quotation("default quotation due to" +ex.getMessage(),11);
                    }
                }).thenApply(identityAndPrinter()).thenAccept(System.out::println).join();


        System.out.println("=============supplier failed. handling experiment exception");
        CompletableFuture<Quotation> quoteFutureExperiment = CompletableFuture.supplyAsync(prepare_supplier("Quotation5", null));
        quoteFutureExperiment
                .<Optional<Quotation>>handle((value, ex) -> {
                    if (ex == null) {
                        System.out.println("*******handle - OK");
                        return Optional.of(value);
                    } else {
                        System.out.println("*******handle - ex:"+ex.getMessage());
                        return Optional.empty();
                    }
                }).thenApply(identityAndPrinter()).thenAccept(System.out::println).join();

        System.out.println("=============supplier handling experiment OK");
        CompletableFuture<Quotation> quoteFutureExperimentOk = CompletableFuture.supplyAsync(prepare_supplier("Quotation6", 111));
        quoteFutureExperimentOk
                .<Optional<Quotation>>handle((value, ex) -> {
                    if (ex == null) {
                        System.out.println("*******handle - OK");
                        return Optional.of(value);
                    } else {
                        System.out.println("*******handle - ex:"+ex.getMessage());
                        return Optional.empty();
                    }
                }).thenApply(identityAndPrinter()).thenAccept(System.out::println).join();


        System.out.println("=============supplier failed. whencomplete does not change");
        CompletableFuture<Quotation> futureFailedWhencomplete = CompletableFuture.supplyAsync(prepare_supplier("Quotation7", null));
        try {
            futureFailedWhencomplete.thenApply(identityAndPrinter())
                    .whenComplete((value, ex) -> {
                        System.out.println("whenComplete " + value + ex.getMessage());
                    })
                    .join();
        } catch (Exception ex) {
            System.out.println("PIPELINE:" + ex.getMessage());
        }


        System.out.println("=============supplier OK. whencomplete fails on same executor");
        CompletableFuture<Quotation> futureOKWhencompleteFails = CompletableFuture.supplyAsync(prepare_supplier("Quotation8", 111));
        try {
            futureOKWhencompleteFails.thenApply(identityAndPrinter())
                    .whenComplete((value, ex) -> {
                        System.out.println("whenComplete " + value + " ex:"+ex + "...throwing NPE");
                        throw new NullPointerException("whenComplete 000000000 ");
                    })
                    .thenAccept(System.out::println)
                    .join();
        } catch (Exception ex) {
            System.out.println("PIPELINE:" + ex.getMessage());
        }


        System.out.println("=============supplier OK");
        CompletableFuture<Quotation> futureOK = CompletableFuture.supplyAsync(prepare_supplier("Quotation8", 111));
        try {
            futureOKWhencompleteFails.thenApply(identityAndPrinter())
                    .whenCompleteAsync((value, ex) -> {
                        System.out.println("whenComplete " + value + " ex:"+ex );
                    }, SwingUtilities::invokeLater)
                    .thenAcceptAsync(System.out::println)
                    .join();
        } catch (Exception ex) {
            System.out.println("PIPELINE:" + ex.getMessage());
        }
    }
}
