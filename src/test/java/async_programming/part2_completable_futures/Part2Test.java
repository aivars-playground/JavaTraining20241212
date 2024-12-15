package async_programming.part2_completable_futures;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Part2Test {

    record Quotation(String name, Integer price) {}
    static Supplier<Quotation> prepare_supplier(String name, Integer price) {
                return () -> {
                    var sleepTime = new Random().nextInt(100);
                    try {Thread.sleep(sleepTime);} catch (InterruptedException e) { e.printStackTrace(); throw new RuntimeException(e);}
                    System.out.println("-Supplier--return:"+name + " sleepTime:"+sleepTime);
                    return new Quotation(name, price);
                };
    }


    @Test
    void pipeline_test() throws InterruptedException {

        List<Supplier<Quotation>> tasks = List.of(
                prepare_supplier("task1", 100),
                prepare_supplier("task2", 150),
                prepare_supplier("task3", 1000)
        );

        List<CompletableFuture<Quotation>> futures = new ArrayList<>();

        for (Supplier<Quotation> task : tasks) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            futures.add(future);
        }

        Collection<Quotation> quotations = new ConcurrentLinkedDeque<>();
        List<CompletableFuture<Void>> voidFutures = new ArrayList<>();
        for (CompletableFuture<Quotation> future : futures) {

            future.thenAccept(System.out::println);

            CompletableFuture<Void> accept =
                    future.thenAccept(quotations::add); //this needs a threadsafe implementation (sync or Concurrent)

            voidFutures.add(accept);
        }

        for (CompletableFuture<Void> future : voidFutures) {
            //blocking thread to make sure the futures complete
            future.join();
        }
        System.out.println(quotations);

    }

    record WeatherForecast(String server, int temperature) {}
    static Supplier<WeatherForecast> prepare_forecast_supplier(String server, Integer temperature) {
        return () -> {
            var sleepTime = new Random().nextInt(200);
            try {Thread.sleep(sleepTime);} catch (InterruptedException e) { e.printStackTrace(); throw new RuntimeException(e);}
            System.out.println("-Supplier--return:"+server + " sleepTime:"+sleepTime);
            return new WeatherForecast(server, temperature);
        };
    }

    @Test
    void test_future_split_get_all() throws InterruptedException {
        CompletableFuture<Quotation> supplier1 = CompletableFuture.supplyAsync(prepare_supplier("supplier1_price", 100));
        CompletableFuture<Quotation> supplier2 = CompletableFuture.supplyAsync(prepare_supplier("supplier2_price", 101));
        CompletableFuture<Quotation> supplier3 = CompletableFuture.supplyAsync(prepare_supplier("supplier3_price", 200));

        List<CompletableFuture<Quotation>> futures = List.of(supplier1,supplier2,supplier3);
        CompletableFuture[] array = futures.toArray(CompletableFuture[]::new);  //loosing type!!!!

        CompletableFuture<Void> allOf = CompletableFuture.allOf(array);  //array as vararg

        Quotation bestResult = allOf.thenApply(v ->   //ignore v, it is marker of completion
            futures.stream()
                    .map(CompletableFuture::join)
                    .min(Comparator.comparing(Quotation::price))
                    .orElseThrow()

        ).join();

        System.out.println("bestResult:"+bestResult);

    }



    @Test
    void test_future_split_get_all_mix() throws InterruptedException {
        CompletableFuture<Quotation> quotation = CompletableFuture.supplyAsync(prepare_supplier("supplier1_price", 100));
        CompletableFuture<WeatherForecast> forecast = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_1", 101));

        List<CompletableFuture<?>> futures = List.of(quotation,forecast);
        CompletableFuture[] array = futures.toArray(CompletableFuture[]::new);  //loosing type!!!!

        CompletableFuture<Void> allOf = CompletableFuture.allOf(array);  //array as vararg

        Stream<?> streamOfDifferentTypes = allOf.thenApply(v ->   //ignore v, it is marker of completion
                futures.stream()
                        .map(CompletableFuture::join)
        ).join();

        streamOfDifferentTypes.forEach(System.out::println);

        System.out.println("individual results");
        quotation.thenAccept(System.out::println);
        forecast.thenAccept(System.out::println);

    }




    @Test
    void test_future_split_get_any_of() throws InterruptedException {
        CompletableFuture<WeatherForecast> supplier1 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_1", 101));
        CompletableFuture<WeatherForecast> supplier2 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_2", 102));
        CompletableFuture<WeatherForecast> supplier3 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_3", 103));

        List<CompletableFuture<WeatherForecast>> futures = List.of(supplier1,supplier2,supplier3);
        CompletableFuture[] array = futures.toArray(CompletableFuture[]::new);

        CompletableFuture<Object> response = CompletableFuture.anyOf(array);

        //should return one of the fastests results (not precise!!!)
        response.thenAccept(System.out::println).join();
    }


    @Test
    void test_future_join_bad_example() throws InterruptedException {


        CompletableFuture<WeatherForecast> supplier1 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_1", 101));
        CompletableFuture<WeatherForecast> supplier2 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_2", 102));
        CompletableFuture<WeatherForecast> supplier3 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_3", 103));

        List<CompletableFuture<WeatherForecast>> futures = List.of(supplier1,supplier2,supplier3);
        CompletableFuture[] array = futures.toArray(CompletableFuture[]::new);

        CompletableFuture<Object> todayReport       = CompletableFuture.anyOf(array);
        CompletableFuture<Object> tomorrowReport    = CompletableFuture.anyOf(array);


        //not good, blocks current thread!!!!!!!!!!!!!
        System.out.println("todayReport:"+(WeatherForecast)todayReport.join() + " tomorrowReport:"+(WeatherForecast)tomorrowReport.join());

    }


    @Test
    void test_future_join_combiner() throws InterruptedException {

        CompletableFuture<WeatherForecast> supplier1 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_1", 101));
        CompletableFuture<WeatherForecast> supplier2 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_2", 102));
        CompletableFuture<WeatherForecast> supplier3 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_3", 103));

        List<CompletableFuture<WeatherForecast>> futures = List.of(supplier1,supplier2,supplier3);
        CompletableFuture[] array = futures.toArray(CompletableFuture[]::new);

        CompletableFuture<WeatherForecast> todayReport       = CompletableFuture.anyOf(array).thenApply(o -> (WeatherForecast)o);
        CompletableFuture<WeatherForecast> tomorrowReport    = CompletableFuture.anyOf(array).thenApply(o -> (WeatherForecast)o);


        var combinedReport = todayReport.thenCombine(tomorrowReport, (today,tomorrow) -> "todayReport:"+todayReport + " tomorrowReport:"+tomorrowReport);
        combinedReport.thenAccept(System.out::println).join();
    }

    @Test
    void test_future_join_compose() throws InterruptedException {

        CompletableFuture<WeatherForecast> supplier1 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_1", 101));
        CompletableFuture<WeatherForecast> supplier2 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_2", 102));
        CompletableFuture<WeatherForecast> supplier3 = CompletableFuture.supplyAsync(prepare_forecast_supplier("server_3", 103));

        List<CompletableFuture<WeatherForecast>> futures = List.of(supplier1,supplier2,supplier3);
        CompletableFuture[] array = futures.toArray(CompletableFuture[]::new);

        CompletableFuture<WeatherForecast> todayReport       = CompletableFuture.anyOf(array).thenApply(o -> (WeatherForecast)o);
        CompletableFuture<WeatherForecast> tomorrowReport    = CompletableFuture.anyOf(array).thenApply(o -> (WeatherForecast)o);

        todayReport.thenCompose(
                td -> tomorrowReport.thenApply(tm -> "todayReport:"+td + " tomorrowReport:"+tm)
        ).thenAccept(System.out::println).join();


        //second call will be evaluated only if first succeed
        //pick composition over combination due to performance (fast fail)
    }





}
