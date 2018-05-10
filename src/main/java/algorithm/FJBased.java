package algorithm;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FJBased {
    private final int parallelism;
    private final boolean shuffle;
    private final int thresholdSize;

    public FJBased(int parallelism, boolean shuffle, int thresholdSize) {
        this.parallelism = parallelism;
        this.shuffle = shuffle;
        this.thresholdSize = thresholdSize;
    }

    public static void main(String... args) {
        long start = System.nanoTime();
        BigInteger result = new FJBased(3, true, 1000).factorial(10000000);
        long end = System.nanoTime();
        System.out.println("Bit count: " + result.bitCount());
        System.out.println("Time " + TimeUnit.NANOSECONDS.toSeconds(end-start) + " sec");
    }

    public BigInteger factorial(int n) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        List<BigInteger> sequence = IntStream.rangeClosed(1, n)
                .mapToObj(BigInteger::valueOf)
                .collect(Collectors.toList());
        if (shuffle) {
            Collections.shuffle(sequence, new Random(1000000));
        }
        BigInteger result = pool.invoke(new Computation(thresholdSize, sequence, 0, n, pool));
        return result;
    }

    private static class Computation extends RecursiveTask<BigInteger> {
        private final int thresholdSize;
        private final List<BigInteger> sequence;
        private final int start;
        private final int end;
        private final ForkJoinPool pool;

        public Computation(int thresholdSize, List<BigInteger> sequence, int start, int end, ForkJoinPool pool) {
            this.thresholdSize = thresholdSize;
            this.sequence = sequence;
            this.start = start;
            this.end = end;
            this.pool = pool;
        }

        @Override
        public String toString() {
            return String.format("Computation{start=%d, end=%d}", start, end);
        }

        @Override
        protected BigInteger compute() {
            BigInteger result;
            if (end - start > thresholdSize) {
                Computation left = new Computation(thresholdSize, sequence, start, start + (end - start) / 2, pool);
                Computation right = new Computation(thresholdSize, sequence, start + (end - start) / 2, end, pool);
                pool.execute(left);
                pool.execute(right);
                result = left.join().multiply(right.join());
            } else {
                result = BigInteger.ONE;
                for (int i = start; i < end; i++) {
                    result = result.multiply(sequence.get(i));
                }
            }
            return result;
        }
    }
}
