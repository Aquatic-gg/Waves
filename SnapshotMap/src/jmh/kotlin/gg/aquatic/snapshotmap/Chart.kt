package gg.aquatic.snapshotmap

import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.style.Styler
import java.io.File

/**
 * Reads the JMH JSON output and generates a PNG chart.
 * Completely AI generated, dunno this graphs lib
 */
fun main() {
    val jsonFile = File("build/results/jmh/results.json")
    if (!jsonFile.exists()) {
        println("Results not found. Run ./gradlew jmh first.")
        return
    }

    val content = jsonFile.readText()
    val regex = """"benchmark"\s*:\s*"[^"]+\.([^"]+)",[\s\S]*?"score"\s*:\s*([\d.]+)""".toRegex()

    val results = regex.findAll(content).map {
        BenchmarkData(it.groupValues[1], it.groupValues[2].toDouble())
    }.toList()

    // 1. Single-Threaded Read Comparison
    val singleReadData = results.filter { it.name.startsWith("single") && it.name.contains("Read") }
    saveComparisonChart("Single-Threaded Read Performance", "single_read_results", singleReadData, "Point Read")

    // 2. Single-Threaded Iteration Comparison
    val singleIterData = results.filter { it.name.startsWith("single") && it.name.contains("Iterate") }
    saveComparisonChart("Single-Threaded Iteration Performance", "single_iter_results", singleIterData, "Iteration")

    // 3. Multi-Threaded Point R/W Contention
    val pointData = results.filter { it.name.contains("point_") }
    saveComparisonChart("Multi-Threaded Point R/W", "rw_results", pointData, "Concurrent R/W")

    // 4. Massive Scalability Test
    val scalabilityData = results.filter { it.name.contains("scalability_") }
    saveComparisonChart("Iteration Scalability (7 Readers)", "scalability_results", scalabilityData, "Scalability")
}

data class BenchmarkData(val name: String, val score: Double)

fun saveComparisonChart(title: String, fileName: String, data: List<BenchmarkData>, categoryName: String) {
    if (data.isEmpty()) return

    val chart = CategoryChartBuilder()
        .width(1000).height(600)
        .title(title)
        .xAxisTitle("Implementation")
        .yAxisTitle("Ops/sec (Higher is Better)")
        .build()

    chart.styler.legendPosition = Styler.LegendPosition.InsideNW
    chart.styler.isOverlapped = false

    // Identify scores for different implementations
    val syncScore = data.filter { it.name.contains("snapshot", true) && !it.name.contains("suspending", true) }.sumOf { it.score }
    val mutexScore = data.filter { it.name.contains("suspending", true) }.sumOf { it.score }
    val concurrentScore = data.filter { it.name.contains("concurrent", true) }.sumOf { it.score }
    val hashMapScore = data.filter { it.name.contains("HashMap", true) }.sumOf { it.score }

    if (syncScore > 0) chart.addSeries("SnapshotMap (Sync)", listOf(categoryName), listOf(syncScore))
    if (mutexScore > 0) chart.addSeries("SnapshotMap (Mutex)", listOf(categoryName), listOf(mutexScore))
    if (concurrentScore > 0) chart.addSeries("ConcurrentHashMap", listOf(categoryName), listOf(concurrentScore))
    if (hashMapScore > 0) chart.addSeries("Plain HashMap", listOf(categoryName), listOf(hashMapScore))

    BitmapEncoder.saveBitmap(chart, "./$fileName", BitmapEncoder.BitmapFormat.PNG)
    println("Generated $fileName.png")
}