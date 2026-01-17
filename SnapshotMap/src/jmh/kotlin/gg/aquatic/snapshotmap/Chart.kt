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
    if (!jsonFile.exists()) return

    val content = jsonFile.readText()
    // Extract benchmark name and score
    val regex = """"benchmark"\s*:\s*"[^"]+\.([^"]+)",[\s\S]*?"score"\s*:\s*([\d.]+)""".toRegex()

    val results = regex.findAll(content).map {
        BenchmarkData(it.groupValues[1], it.groupValues[2].toDouble())
    }.toList()

    // Filter by the Group names defined in the Benchmark class
    val pointData = results.filter { it.name.contains("point_") }
    saveComparisonChart("Point R/W Contention", "rw_results", pointData, "Point R/W")

    val heavyData = results.filter { it.name.contains("heavy_") }
    saveComparisonChart("Iteration vs. Continuous Writes", "heavy_iter_results", heavyData, "Heavy Write")

    val scalabilityData = results.filter { it.name.contains("scalability_") }
    saveComparisonChart("Iteration Scalability (1 Write : 6 Readers)", "scalability_results", scalabilityData, "Scalability")

}

data class BenchmarkData(val name: String, val score: Double)

fun saveComparisonChart(title: String, fileName: String, data: List<BenchmarkData>, categoryName: String) {
    val chart = CategoryChartBuilder()
        .width(900).height(600)
        .title(title)
        .xAxisTitle("Implementation")
        .yAxisTitle("Ops/sec")
        .build()

    chart.styler.legendPosition = Styler.LegendPosition.InsideNW

    // Sum scores for the group (JMH reports scores per-method in group, we want the total group throughput)
    val snapshotScore = data.filter { it.name.contains("snapshot", true) }.sumOf { it.score }
    val concurrentScore = data.filter { it.name.contains("concurrent", true) }.sumOf { it.score }

    chart.addSeries("SnapshotMap", listOf(categoryName), listOf(snapshotScore))
    chart.addSeries("ConcurrentHashMap", listOf(categoryName), listOf(concurrentScore))

    BitmapEncoder.saveBitmap(chart, "./$fileName", BitmapEncoder.BitmapFormat.PNG)
    println("Generated $fileName.png")
}