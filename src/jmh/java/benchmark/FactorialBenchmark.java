package benchmark;

import algorithm.TaskBased;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;

public class FactorialBenchmark {

//    @Benchmark
//    public BigInteger threadBased_1e4_t1_tsc10() {
//        TaskBased algorithm = new TaskBased(1, new TaskBased.ConstantTaskSize(10));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t2_tsc10() {
//        TaskBased algorithm = new TaskBased(2, new TaskBased.ConstantTaskSize(10));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t3_tsc10() {
//        TaskBased algorithm = new TaskBased(3, new TaskBased.ConstantTaskSize(10));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t4_tsc10() {
//        TaskBased algorithm = new TaskBased(4, new TaskBased.ConstantTaskSize(10));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t4_tsbcb10() {
//        TaskBased algorithm = new TaskBased(4, new TaskBased.BitCountBasedTaskSize(10));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t4_tsc2() {
//        TaskBased algorithm = new TaskBased(4, new TaskBased.ConstantTaskSize(2));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t4_tsc100() {
//        TaskBased algorithm = new TaskBased(4, new TaskBased.ConstantTaskSize(100));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t10_tsc10() {
//        TaskBased algorithm = new TaskBased(10, new TaskBased.ConstantTaskSize(10));
//        return algorithm.factorial(10000);
//    }
//
//    @Benchmark
//    public BigInteger threadBased_1e4_t100_tsc10() {
//        TaskBased algorithm = new TaskBased(100, new TaskBased.ConstantTaskSize(10));
//        return algorithm.factorial(10000);
//    }

    @Benchmark
    public BigInteger threadBased_1e6_t1_tsbcb10() {
        TaskBased algorithm = new TaskBased(1, new TaskBased.BitCountBasedTaskSize(10));
        return algorithm.factorial(1000000);
    }

    @Benchmark
    public BigInteger threadBased_1e6_t2_tsbcb10() {
        TaskBased algorithm = new TaskBased(2, new TaskBased.BitCountBasedTaskSize(10));
        return algorithm.factorial(1000000);
    }

    @Benchmark
    public BigInteger threadBased_1e6_t3_tsbcb10() {
        TaskBased algorithm = new TaskBased(3, new TaskBased.BitCountBasedTaskSize(10));
        return algorithm.factorial(1000000);
    }

    @Benchmark
    public BigInteger threadBased_1e6_t4_tsbcb10() {
        TaskBased algorithm = new TaskBased(4, new TaskBased.BitCountBasedTaskSize(10));
        return algorithm.factorial(1000000);
    }

    @Benchmark
    public BigInteger threadBased_1e6_t4_tsbcb100() {
        TaskBased algorithm = new TaskBased(4, new TaskBased.BitCountBasedTaskSize(100));
        return algorithm.factorial(1000000);
    }

    @Benchmark
    public BigInteger threadBased_1e6_t10_tsbcb10() {
        TaskBased algorithm = new TaskBased(10, new TaskBased.BitCountBasedTaskSize(10));
        return algorithm.factorial(1000000);
    }

    @Benchmark
    public BigInteger threadBased_1e6_t100_tsbcb10() {
        TaskBased algorithm = new TaskBased(100, new TaskBased.BitCountBasedTaskSize(10));
        return algorithm.factorial(1000000);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FactorialBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
