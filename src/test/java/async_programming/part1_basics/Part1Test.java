package async_programming.part1_basics;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

class Part1Test {

    @Test
    public void test_future_do_work_during_execution() throws ExecutionException, InterruptedException {
        System.out.println("----1");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        System.out.println("----2 before future execution");
        Future<Integer> future = executor.submit(() -> {
            System.out.println("-thread---start");
            Thread.sleep(5);
            System.out.println("-thread---end");
            return 42;
        });
        System.out.println("----3 after future execution");
        while (!future.isDone()) {
            System.out.println("----4:block main thread during execution");
            Thread.sleep(1);
        }
        System.out.println("----5:future returned "+future.get());

    }

    static Runnable  prepare_runnable(String name, Integer weight, List<Result> accumulator) {
        return () -> {
            System.out.println("-Runnable:"+name+":start");
            var sleeptime = new Random().nextInt(10);
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("-Runnable:"+name+":end, sleeptime:"+sleeptime+"ms");
            accumulator.add(new Result("name:"+name+":success @"+weight, weight));
        };
    }

    @Test
    public void test_runnable() throws InterruptedException {
        List<Result> accumulator = Collections.synchronizedList(new ArrayList<>());
        Thread t = new Thread(prepare_runnable("aaa",1,accumulator));

        Instant start = Instant.now();
        System.out.println("---start, state:"+t.getState());
        t.start();
        System.out.println("---in progress, state:"+t.getState());

        while (t.getState() != Thread.State.TERMINATED) {
            System.out.println("---in progress, monitoring:"+t.getState());
            Thread.sleep(1);
        }

        Result res = accumulator.stream()
                .max(Comparator.comparing(Result::weight))
                .orElseThrow();

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:" + res);

    }

    @Test
    public void test_runnables() throws InterruptedException {
        List<Result> accumulator = Collections.synchronizedList(new ArrayList<>());
        List<Runnable> tasks = List.of(
                prepare_runnable("c1",1111,accumulator),
                prepare_runnable("c2",111,accumulator),
                prepare_runnable("c3",11,accumulator),
                prepare_runnable("c4",1,accumulator)
        );

        Instant start = Instant.now();
        System.out.println("---start");

        List<Thread> threads = new ArrayList<>();
        for (Runnable runnable : tasks) {
            threads.add(new Thread(runnable));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        while (threads.stream().anyMatch(t -> t.getState() != Thread.State.TERMINATED)) {
            System.out.println("---waiting");
            Thread.sleep(1);
        }

        Result res = accumulator.stream()
                .max(Comparator.comparing(Result::weight))
                .orElseThrow();

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:" + res);
    }



    @Test
    public void test_runnables_in_executor() throws InterruptedException, ExecutionException {
        List<Result> accumulator = Collections.synchronizedList(new ArrayList<>());
        Runnable task =  prepare_runnable("c1",1111,accumulator);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Instant start = Instant.now();
        System.out.println("---start");

        Future ft = executor.submit(task);
        System.out.println("---"+ft.state());
        while (!ft.isDone()) {
            System.out.println("---"+ft.state());
            Thread.sleep(1);
        }


        Result res = accumulator.stream()
                .max(Comparator.comparing(Result::weight))
                .orElseThrow();

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:" + res);
    }


    record Result(String result, int weight) {}
    static Callable<Result>  prepare_callable(String name, Integer weight) {
            return
                    () -> {
                        System.out.println("-callable:"+name+":start");
                        var sleeptime = new Random().nextInt(10);
                        Thread.sleep(sleeptime);
                        System.out.println("-callable:"+name+":end, sleeptime:"+sleeptime+"ms");
                        return new Result("name:"+name+":success @"+weight, weight);
                    };
    }

    //wraps checked exception in RuntimeException
    static Result fetchResult(Callable<Result> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_future_callables_synchronously() throws ExecutionException, InterruptedException {

        List<Callable<Result>> tasks = List.of(
                prepare_callable("c1",1111),
                prepare_callable("c2",111),
                prepare_callable("c3",11),
                prepare_callable("c4",1)
        );

        Instant start = Instant.now();
        System.out.println("---start");

        Result heaviestItem =
            tasks.stream()
                    .map(task -> fetchResult(task))
                    .max(Comparator.comparing(Result::weight))
                    .orElseThrow(); //this time stream not to be empty!!!

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:"+heaviestItem);
        //ALL CALLS MADE SYNCHRONOUSLY, SLOW

    }

    @Test
    public void test_future_callables_asynchronously_blocking_res() throws ExecutionException, InterruptedException {

        List<Callable<Result>> tasks = List.of(
                prepare_callable("c1",1111),
                prepare_callable("c2",111),
                prepare_callable("c3",11),
                prepare_callable("c4",1)
        );

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Instant start = Instant.now();
        System.out.println("---start");

        List<Future<Result>> futures = new ArrayList<>();
        for (Callable<Result> callable : tasks) {
            Future<Result> future = executor.submit(callable);
            System.out.println("created "  + future);
            futures.add(future);
        }

        List<Result> results = new ArrayList<>();
        for (Future<Result> future : futures) {
            System.out.println("---waiting"+future);
            Result result = future.get();
            System.out.println("---got"+future);
            results.add(result);
        }

        Result res = results.stream()
                .max(Comparator.comparing(Result::weight))
                .orElseThrow();

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:" + res);

    }


    @Test
    public void test_future_callables_asynchronously_invoke_all() throws ExecutionException, InterruptedException {


        List<Callable<Result>> tasks = List.of(
                prepare_callable("c1",1111),
                prepare_callable("c2",111),
                prepare_callable("c3",11),
                prepare_callable("c4",1)
        );

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Instant start = Instant.now();
        System.out.println("---start");

        List<Future<Result>> futures = executor.invokeAll(tasks);

        List<Result> results = new ArrayList<>();
        for (Future<Result> future : futures) {
            System.out.println("---waiting"+future);
            Result result = future.get();
            System.out.println("---got"+future);
            results.add(result);
        }

        Result res = results.stream()
                .max(Comparator.comparing(Result::weight))
                .orElseThrow();

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:" + res);

    }

    static Supplier<Result> prepare_supplier(String name, Integer weight) {
        return
                () -> {
                    System.out.println("-callable:"+name+":start");
                    var sleeptime = new Random().nextInt(10);
                    try {
                        Thread.sleep(sleeptime);
                    } catch (InterruptedException e) {
                        System.out.println("============OUCH"+e);
                        throw new RuntimeException(e);
                    }
                    System.out.println("-callable:"+name+":end, sleeptime:"+sleeptime+"ms");
                    return new Result("name:"+name+":success @"+weight, weight);
                };
    }


    @Test
    public void test_completionService() throws ExecutionException, InterruptedException {
        List<Supplier<Result>> tasks = List.of(
                prepare_supplier("c1",1111),
                prepare_supplier("c2",111),
                prepare_supplier("c3",11),
                prepare_supplier("c4",1)
        );



        ExecutorService executor = Executors.newFixedThreadPool(4);

        Instant start = Instant.now();
        System.out.println("---start");

        List<CompletableFuture<Result>> futures = new ArrayList<>();
        for (Supplier<Result> supplierTask : tasks) {
            CompletableFuture<Result> completableFuture =  CompletableFuture.supplyAsync(supplierTask);
            futures.add(completableFuture);
        }

        List<Result> results = new ArrayList<>();
        for (CompletableFuture<Result> future : futures) {
            Result result = future.join();
            results.add(result);
        }

        Result res = results.stream()
                .max(Comparator.comparing(Result::weight))
                .orElseThrow();

        Instant finish = Instant.now();
        System.out.println("---end in:"+ Duration.between(start, finish).toMillis() +"ms result:" + res);


    }


}
