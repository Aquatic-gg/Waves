package gg.aquatic.shardedmap

import org.openjdk.jmh.annotations.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.LongAdder

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class ShardedMapBenchmark {

    @Param("100000")
    var mapSize: Int = 0

    private lateinit var shardedMap: ShardedMap<Int, Int>
    private lateinit var concurrentMap: ConcurrentHashMap<Int, Int>

    @Setup
    fun setup() {
        shardedMap = ShardedMap(256)
        concurrentMap = ConcurrentHashMap()

        for (i in 0 until mapSize) {
            shardedMap[i] = i
            concurrentMap[i] = i
        }
    }

    @Benchmark
    @Group("sharded_rw")
    @GroupThreads(4) // 4 threads reading
    fun shardedRead(): Int? = shardedMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    @Group("sharded_rw")
    @GroupThreads(1) // 1 thread writing simultaneously
    fun shardedWrite() {
        val key = ThreadLocalRandom.current().nextInt(mapSize)
        shardedMap[key] = key
    }

    @Benchmark
    @Group("concurrent_rw")
    @GroupThreads(4)
    fun concurrentRead(): Int? = concurrentMap[ThreadLocalRandom.current().nextInt(mapSize)]

    @Benchmark
    @Group("concurrent_rw")
    @GroupThreads(1)
    fun concurrentWrite() {
        val key = ThreadLocalRandom.current().nextInt(mapSize)
        concurrentMap[key] = key
    }

    @Benchmark
    fun testShardedForEach(): Long {
        val adder = LongAdder()
        shardedMap.forEach { _, v -> adder.add(v.toLong()) }
        return adder.sum()
    }

    @Benchmark
    fun testConcurrentForEach(): Long {
        val adder = LongAdder()
        concurrentMap.forEach { k, v -> adder.add(v.toLong()) }
        return adder.sum()
    }
}
