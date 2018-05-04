import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThreadBased {
    private final int parallelism;
    private final int taskSize;

    private static class SubTask {
        private final List<BigInteger> sequence;

        public SubTask(List<BigInteger> sequence) {
            this.sequence = sequence;
        }

        BigInteger compute() {
            BigInteger product = BigInteger.ONE;
            for (int i = 0; i < sequence.size(); i++) {
                product = product.multiply(sequence.get(i));
            }
            return product;
        }
    }

    private class Computation implements Runnable {
        private int index;
        private final Queue<SubTask> tasks;
        private final List<BigInteger> results;

        Computation(int index, Queue<SubTask> tasks, List<BigInteger> results) {
            this.index = index;
            this.tasks = tasks;
            this.results = results;
        }
        @Override
        public void run() {
            System.out.println("Starting: " + index + ": " + System.currentTimeMillis());
            LinkedList<BigInteger> localResults = new LinkedList<>();
            SubTask task;
            while ((task = getTask()) != null) {
                BigInteger taskResult = task.compute();
                localResults.add(taskResult);
            }
            synchronized (results) {
                results.addAll(localResults);
            }
            System.out.println("Completed: " + index + ": " + System.currentTimeMillis());
        }

        private SubTask getTask() {
            synchronized (tasks) {
                return tasks.poll();
            }
        }
    }

    ThreadBased() {
        this(Runtime.getRuntime().availableProcessors(), 10);
    }

    ThreadBased(int parallelism, int taskSize) {
        this.parallelism = parallelism;
        this.taskSize = taskSize;
    }

    private Queue<SubTask> splitWork(List<BigInteger> sequence, int taskSize) {
        Queue<SubTask> subTasks = new LinkedList<>();
        int n = sequence.size();
        for (int i = 0; i < n /taskSize + 1; i++) {
            int start = i * taskSize;
            int end = (i + 1) * taskSize;
            if (start < n && end <= n) {
                subTasks.add(new SubTask(sequence.subList(start, end)));
            } else if (start < n && end > n) {
                subTasks.add(new SubTask(sequence.subList(start, n + 1)));
            }
        }
        return subTasks;
    }

    public BigInteger factorial(int n) {
        List<BigInteger> sequence = IntStream.rangeClosed(1, n).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        Queue<SubTask> subTasks = splitWork(sequence, taskSize);
        long start = System.nanoTime();
        List<BigInteger> taskResults = compute(subTasks);
        System.out.println("Time to compute first subTasks " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start) + " sec");
        subTasks = splitWork(taskResults, taskSize);
        start = System.nanoTime();
        taskResults = compute(subTasks);
        System.out.println("Time to compute second subTasks " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start) + " sec");
        subTasks = splitWork(taskResults, taskSize/10);
        start = System.nanoTime();
        taskResults = compute(subTasks);
        System.out.println("Time to compute third subTasks " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start) + " sec");
        subTasks = splitWork(taskResults, 2);
        start = System.nanoTime();
        taskResults = compute(subTasks);
        System.out.println("Time to compute fourth subTasks " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start) + " sec");
        SubTask finalTask = new SubTask(taskResults);
        BigInteger result = finalTask.compute();
        System.out.println("Time to compute final result " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start) + " sec");
        return result;
    }

    private List<BigInteger> compute(Queue<SubTask> subTasks) {
        List<BigInteger> taskResults = new LinkedList<>();
        List<Thread> computations = new ArrayList<>();
        for (int i = 0; i < parallelism; i++) {
            Computation computation = new Computation(i, subTasks, taskResults);
            Thread thread = new Thread(computation, "computation-" + i);
            computations.add(thread);
            thread.start();
        }
        for (int i = 0; i < computations.size(); i++) {
            try {
                computations.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return taskResults;
    }

    public static void main(String... args) {
        long start = System.nanoTime();
        BigInteger result = new ThreadBased(3, 100).factorial(10000000);
        long end = System.nanoTime();
        System.out.println(result.bitCount());
        System.out.println("Time " + TimeUnit.NANOSECONDS.toSeconds(end-start) + " sec");
    }
}
