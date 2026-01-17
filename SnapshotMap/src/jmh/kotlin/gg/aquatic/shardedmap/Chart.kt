package gg.aquatic.shardedmap

import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.style.Styler
import java.io.File

/**
 * Reads the JMH JSON output and generates a PNG chart.
 */
fun main() {
    val jsonFile = File("build/results/jmh/results.json")
    if (!jsonFile.exists()) return

    val content = jsonFile.readText()
    val regex = """"benchmark"\s*:\s*"[^"]+\.([^"]+)",[\s\S]*?"score"\s*:\s*([\d.]+)""".toRegex()
    val allResults = regex.findAll(content).map { it.groupValues[1] to it.groupValues[2].toDouble() }.toList()

    // 1. Filter for Iteration (Fast)
    val iterationResults = allResults.filter { it.first.contains("ForEach", ignoreCase = true) }
    saveChart("Iteration Performance", "iteration_results", iterationResults)

    // 2. Filter for Read/Write (Heavy)
    val rwResults = allResults.filter { !it.first.contains("ForEach", ignoreCase = true) }
    saveChart("Read/Write Contention", "rw_results", rwResults)
}

fun saveChart(title: String, fileName: String, data: List<Pair<String, Double>>) {
    if (data.isEmpty()) return

    val chart = CategoryChartBuilder()
        .width(800).height(500)
        .title(title)
        .xAxisTitle("Operation")
        .yAxisTitle("Ops/sec")
        .build()

    chart.styler.legendPosition = Styler.LegendPosition.InsideNW
    chart.styler.labelsRotation = 45

    chart.addSeries("Throughput", data.map { it.first }, data.map { it.second })

    BitmapEncoder.saveBitmap(chart, "./$fileName", BitmapEncoder.BitmapFormat.PNG)
    println("Saved $fileName.png")
}
