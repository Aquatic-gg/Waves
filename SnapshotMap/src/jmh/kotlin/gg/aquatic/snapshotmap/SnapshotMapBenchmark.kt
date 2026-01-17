package gg.aquatic.snapshotmap

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.Collections
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
    private lateinit var plainHashMap: HashMap<Int, Int>

    @Setup
    fun setup() {
        snapshotMap = SnapshotMap(ConcurrentHashMap(131072))
        concurrentMap = ConcurrentHashMap(131072)
        plainHashMap = HashMap(131072)

        for (i in 0 until mapSize) {
            snapshotMap[i] = i
            concurrentMap[i] = i
            plainHashMap[i] = i
        }

        // Pre-build snapshot to measure "Hot" iteration performance
        snapshotMap.forEach { _, _ -> }
    }

    // --- Single-Threaded Benchmarks ---

    @Benchmark
    fun singleSnapshotRead(): Int? = snapshotMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    fun singleHashMapRead(): Int? = plainHashMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    fun singleSnapshotIterate(bh: Blackhole) {
        snapshotMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    fun singleHashMapIterate(bh: Blackhole) {
        plainHashMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    // --- Multi-Threaded Point Lookups (Read 4 : Write 1) ---

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

    // --- Scalability: Rare Writes (Iteration Heavy) ---

    @Benchmark
    @Group("scalability_snapshot")
    @GroupThreads(7)
    fun snapshotScalability(bh: Blackhole) {
        snapshotMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    @Group("scalability_snapshot")
    @GroupThreads(1)
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
}