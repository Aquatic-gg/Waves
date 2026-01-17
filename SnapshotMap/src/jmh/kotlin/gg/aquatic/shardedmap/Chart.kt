package gg.aquatic.shardedmap

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
    val regex = """"benchmark"\s*:\s*"[^"]+\.([^"]+)",[\s\S]*?"mode"\s*:\s*"([^"]+)",[\s\S]*?"score"\s*:\s*([\d.]+)""".toRegex()

    val allResults = regex.findAll(content).map {
        val name = it.groupValues[1]
        val mode = it.groupValues[2]
        val score = it.groupValues[3].toDouble()
        BenchmarkData(name, mode, score)
    }.toList()

    val throughputResults = allResults.filter { it.mode == "thrpt" }

    // Group ForEach results into a single "Iteration" category
    val iterationData = throughputResults.filter { it.name.contains("ForEach", ignoreCase = true) }
    saveComparisonChart("Iteration Performance (Throughput)", "iteration_results", iterationData, "Iteration")

    // Group Read/Write results
    val rwData = throughputResults.filter { !it.name.contains("ForEach", ignoreCase = true) }
    saveComparisonChart("Read/Write Contention (Throughput)", "rw_results", rwData)
}

data class BenchmarkData(val name: String, val mode: String, val score: Double)

/**
 * Saves a comparison chart. If [overrideCategory] is provided, all data points are grouped
 * under that single X-axis label.
 */
fun saveComparisonChart(title: String, fileName: String, data: List<BenchmarkData>, overrideCategory: String? = null) {
    if (data.isEmpty()) return

    val chart = CategoryChartBuilder()
        .width(900).height(600)
        .title(title)
        .xAxisTitle("Operation Type")
        .yAxisTitle("Ops/sec (Higher is Better)")
        .build()

    chart.styler.legendPosition = Styler.LegendPosition.InsideNW
    chart.styler.labelsRotation = 0

    val shardedScores = mutableListOf<Double>()
    val concurrentScores = mutableListOf<Double>()
    val categories = mutableListOf<String>()

    // Determine category names on the X-axis
    val types = if (overrideCategory != null) {
        listOf(overrideCategory)
    } else {
        data.map {
            it.name.replace("test", "")
                .replace("sharded", "")
                .replace("concurrent", "")
                .trim()
        }.distinct().sortedDescending()
    }

    for (type in types) {
        categories.add(type)

        // When overriding, we match based on implementation name regardless of the category suffix
        val searchKey = if (overrideCategory != null) "" else type

        shardedScores.add(data.find { it.name.contains("sharded", true) && it.name.contains(searchKey, true) }?.score ?: 0.0)
        concurrentScores.add(data.find { it.name.contains("concurrent", true) && it.name.contains(searchKey, true) }?.score ?: 0.0)
    }

    chart.addSeries("ShardedMap", categories, shardedScores)
    chart.addSeries("ConcurrentHashMap", categories, concurrentScores)

    BitmapEncoder.saveBitmap(chart, "./$fileName", BitmapEncoder.BitmapFormat.PNG)
    println("Saved $fileName.png with multiple series.")
}