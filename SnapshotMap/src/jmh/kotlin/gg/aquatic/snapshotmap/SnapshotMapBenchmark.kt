package gg.aquatic.snapshotmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

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
    private lateinit var suspendingSnapshotMap: SuspendingSnapshotMap<Int, Int>
    private lateinit var concurrentMap: ConcurrentHashMap<Int, Int>
    private lateinit var plainHashMap: HashMap<Int, Int>

    @Setup
    fun setup() {
        snapshotMap = SnapshotMap(ConcurrentHashMap(131072))
        suspendingSnapshotMap = SuspendingSnapshotMap(ConcurrentHashMap(131072))
        concurrentMap = ConcurrentHashMap(131072)
        plainHashMap = HashMap(131072)

        for (i in 0 until mapSize) {
            snapshotMap[i] = i
            suspendingSnapshotMap[i] = i
            concurrentMap[i] = i
            plainHashMap[i] = i
        }

        snapshotMap.forEach { _, _ -> }
        runBlocking { suspendingSnapshotMap.forEachSuspended { _, _ -> } }
    }

    /**
     * This state is created once per thread, providing a "Native"
     * coroutine environment for the benchmark methods to use.
     */
    @State(Scope.Thread)
    open class ThreadState {
        // Empty context is the fastest for benchmarks as it uses the calling thread
        val scope = CoroutineScope(EmptyCoroutineContext)

        @TearDown
        fun tearDown() {
            scope.cancel()
        }
    }

    // --- Single-Threaded Benchmarks ---

    @Benchmark
    fun singleSnapshotRead(): Int? = snapshotMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    fun singleHashMapRead(): Int? = plainHashMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    fun singleConcurrentMapRead(): Int? = concurrentMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    fun singleSnapshotIterate(bh: Blackhole) {
        snapshotMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
    }

    @Benchmark
    fun singleSuspendingSnapshotIterate(state: ThreadState, bh: Blackhole) {
        // Using 'runTest' or 'runBlocking' on a specific context is the direct way.
        // We use runBlocking here because it's the standard bridge.
        runBlocking(state.scope.coroutineContext) {
            suspendingSnapshotMap.forEachSuspended { k, v -> bh.consume(k); bh.consume(v) }
        }
    }

    @Benchmark
    fun singleHashMapIterate(bh: Blackhole) {
        plainHashMap.forEach { k, v -> bh.consume(k); bh.consume(v) }
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
    @Group("scalability_suspending")
    @GroupThreads(7)
    fun suspendingScalability(state: ThreadState, bh: Blackhole) {
        runBlocking(state.scope.coroutineContext) {
            suspendingSnapshotMap.forEachSuspended { k, v -> bh.consume(k); bh.consume(v) }
        }
    }

    @Benchmark
    @Group("scalability_suspending")
    @GroupThreads(1)
    fun suspendingRareWrite() {
        Thread.sleep(100)
        suspendingSnapshotMap[ThreadLocalRandom.current().nextInt(mapSize)] = 1
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