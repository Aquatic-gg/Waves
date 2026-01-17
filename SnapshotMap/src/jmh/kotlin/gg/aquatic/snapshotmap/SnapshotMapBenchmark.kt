package gg.aquatic.snapshotmap

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class SnapshotMapBenchmark {

    @Param("100000")
    var mapSize: Int = 0

    private lateinit var snapshotMap: SnapshotMap<Int, Int>
    private lateinit var concurrentMap: ConcurrentHashMap<Int, Int>

    @Setup
    fun setup() {
        snapshotMap = SnapshotMap(ConcurrentHashMap(131072))
        concurrentMap = ConcurrentHashMap(131072)

        for (i in 0 until mapSize) {
            snapshotMap[i] = i
            concurrentMap[i] = i
        }
    }

    // --- Standard Point Lookups ---

    @Benchmark
    @Group("point_snapshot")
    @GroupThreads(4)
    fun snapshotRead(): Int? = snapshotMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    @Group("point_snapshot")
    @GroupThreads(1)
    fun snapshotWrite() {
        snapshotMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
    }

    @Benchmark
    @Group("point_concurrent")
    @GroupThreads(4)
    fun concurrentRead(): Int? = concurrentMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    @Group("point_concurrent")
    @GroupThreads(1)
    fun concurrentWrite() {
        concurrentMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
    }

    // --- Scalability: Rare Writes (The SnapshotMap Sweet Spot) ---
    // Here we simulate a system where data changes occasionally (every 100ms)
    // but is iterated constantly by many threads.

    @Benchmark
    @Group("scalability_snapshot")
    @GroupThreads(7) // 7 threads reading
    fun snapshotScalability(bh: Blackhole) {
        snapshotMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    @Group("scalability_snapshot")
    @GroupThreads(1) // 1 thread writing rarely
    fun snapshotRareWrite() {
        Thread.sleep(100)
        snapshotMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
    }

    @Benchmark
    @Group("scalability_concurrent")
    @GroupThreads(7)
    fun concurrentScalability(bh: Blackhole) {
        concurrentMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    @Group("scalability_concurrent")
    @GroupThreads(1)
    fun concurrentRareWrite() {
        Thread.sleep(100)
        concurrentMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
    }

    // --- Stress Test: Continuous Writes ---
    // This will likely favor ConcurrentHashMap due to frequent invalidations.

    @Benchmark
    @Group("heavy_snapshot")
    @GroupThreads(3)
    fun snapshotIterateHeavy(bh: Blackhole) {
        snapshotMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    @Group("heavy_snapshot")
    @GroupThreads(1)
    fun snapshotUpdateHeavy() {
        snapshotMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
    }

    @Benchmark
    @Group("heavy_concurrent")
    @GroupThreads(3)
    fun concurrentIterateHeavy(bh: Blackhole) {
        concurrentMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    @Group("heavy_concurrent")
    @GroupThreads(1)
    fun concurrentUpdateHeavy() {
        concurrentMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
    }
}