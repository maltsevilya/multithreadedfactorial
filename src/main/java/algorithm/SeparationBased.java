package algorithm;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SeparationBased {
    private int parallelism;

    public SeparationBased(int parallelism) {
        this.parallelism = parallelism;
    }

    public static void main(String... args) {
        long start = System.nanoTime();
        BigInteger result = new SeparationBased(4).factorial(1000000);
        long end = System.nanoTime();
        System.out.println("Bit count: " + result.bitCount());
        System.out.println("Time " + TimeUnit.NANOSECONDS.toSeconds(end-start) + " sec");
    }

    public BigInteger factorial(int n) {
        List<BigInteger> sequence = IntStream.rangeClosed(1, n)
                .mapToObj(BigInteger::valueOf)
                .collect(Collectors.toList());
        List<Computation> computations = IntStream.range(0, parallelism)
                .mapToObj(i -> new Computation(i, parallelism, sequence))
                .collect(Collectors.toList());
        computations.forEach(Thread::start);
        for (Computation computation : computations) {
            try {
                computation.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        BigInteger acc = BigInteger.ONE;
        for (Computation computation : computations) {
            BigInteger result = computation.getResult();
            acc = acc.multiply(result);
        }
        return acc;
    }

    private static class Computation extends Thread {
        private final int index;
        private final int parallelism;
        private final List<BigInteger> values;
        private BigInteger result;

        Computation(int index, int parallelism, List<BigInteger> values) {
            super("computation"+index);
            this.index = index;
            this.parallelism = parallelism;
            this.values = values;
        }

        @Override
        public void run() {
            BigInteger accumulator = BigInteger.ONE;
            for (int i = index; i < values.size(); i+=parallelism) {
                accumulator = accumulator.multiply(values.get(i));
            }
            this.result = accumulator;
        }

        BigInteger getResult() {
            return result;
        }
    }
}
