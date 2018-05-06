package algorithm;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;

public class TaskBased {
    private final int parallelism;
    private final TaskSize taskSize;

    private static class SubTask {
        private final List<BigInteger> sequence;

        public SubTask(List<BigInteger> sequence) {
            this.sequence = new ArrayList<>(sequence);
        }

        BigInteger compute() {
            BigInteger product = sequence.get(0);
            if (sequence.size() > 1) {
                for (int i = 1; i < sequence.size(); i++) {
                    product = product.multiply(sequence.get(i));
                }
            }
            return product;
        }
    }

    private class Computation implements Runnable {
        private int index;
        private final Queue<SubTask> tasks;
        private final List<BigInteger> results;
        private final TaskSize taskSize;

        Computation(int index, Queue<SubTask> tasks, List<BigInteger> results, TaskSize taskSize) {
            this.index = index;
            this.tasks = tasks;
            this.results = results;
            this.taskSize = taskSize;
        }

        @Override
        public void run() {
//            System.out.println("Starting: " + index + ": " + System.currentTimeMillis());
            ArrayList<BigInteger> localResults = new ArrayList<>();
            SubTask task;
            int nextTaskSize = taskSize.getTaskSize(emptyList());
            while ((task = getTask()) != null) {
                BigInteger taskResult = task.compute();
                localResults.add(taskResult);
                if (localResults.size() == nextTaskSize) {
                    SubTask newTask = new SubTask(localResults);
                    synchronized (tasks) {
                        tasks.add(newTask);
                    }
                    nextTaskSize = taskSize.getTaskSize(localResults);
                    localResults.clear();
                }
            }
            if (!localResults.isEmpty()) {
                synchronized (results) {
                    results.addAll(localResults);
                }
            }
//            System.out.println("Completed: " + index + ": " + System.currentTimeMillis());
        }

        private SubTask getTask() {
            synchronized (tasks) {
                return tasks.poll();
            }
        }
    }

    interface TaskSize {
        int getTaskSize(List<BigInteger> data);
    }

    public static class ConstantTaskSize implements TaskSize {
        private final int size;

        public ConstantTaskSize(int size) {
            this.size = size;
        }

        @Override
        public int getTaskSize(List<BigInteger> data) {
            return size;
        }
    }

    public static class BitCountBasedTaskSize implements TaskSize {
        private final int startSize;

        public BitCountBasedTaskSize(int startSize) {
            this.startSize = startSize;
        }

        @Override
        public int getTaskSize(List<BigInteger> data) {
            int maxBitCount = data.stream().mapToInt(BigInteger::bitCount).max().orElse(10);
            int result = maxBitCount != 0 ? Math.max(2, (int) (startSize / Math.log(startSize * startSize * maxBitCount))) : startSize;
            return result;
        }
    }

    public TaskBased() {
        this(Runtime.getRuntime().availableProcessors() - 1, new ConstantTaskSize(10));
    }

    public TaskBased(int parallelism, TaskSize taskSize) {
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
                subTasks.add(new SubTask(sequence.subList(start, n)));
            }
        }
        return subTasks;
    }

    public BigInteger factorial(int n) {
        List<BigInteger> sequence = IntStream.rangeClosed(1, n).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        Queue<SubTask> subTasks = splitWork(sequence, taskSize.getTaskSize(sequence));
        long start = System.nanoTime();
        List<BigInteger> taskResults = compute(subTasks, taskSize);
//        System.out.println("SubTask results count " + taskResults.size());
        subTasks = splitWork(taskResults, 2);
        taskResults = compute(subTasks, new ConstantTaskSize(2));
//        System.out.println("SubTask results count " + taskResults.size());
        BigInteger result = new SubTask(taskResults).compute();
//        System.out.println("Time to compute final result " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start) + " sec");
        return result;
    }

    private List<BigInteger> compute(Queue<SubTask> subTasks, TaskSize taskSize) {
        List<BigInteger> taskResults = new ArrayList<>();
        List<Thread> computations = new ArrayList<>();
        for (int i = 0; i < parallelism; i++) {
            Computation computation = new Computation(i, subTasks, taskResults, taskSize);
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
        BigInteger result = new TaskBased(4, new BitCountBasedTaskSize(10)).factorial(1000000);
        long end = System.nanoTime();
        System.out.println("Bit count: " + result.bitCount());
        System.out.println("Time " + TimeUnit.NANOSECONDS.toSeconds(end-start) + " sec");
    }
}
