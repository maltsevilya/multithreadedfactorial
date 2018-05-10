package benchmark;

import algorithm.FJBased;
import algorithm.TaskBased;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;

public class FJBasedBenchmark {

    @Benchmark
    public BigInteger fj_p3_shuffle() {
        FJBased algorithm = new FJBased(3, true, 1000);
        return algorithm.factorial(10000000);
    }

    @Benchmark
    public BigInteger fj_p3_nonshuffle() {
        FJBased algorithm = new FJBased(3, false, 1000);
        return algorithm.factorial(10000000);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FJBasedBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(20)
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
