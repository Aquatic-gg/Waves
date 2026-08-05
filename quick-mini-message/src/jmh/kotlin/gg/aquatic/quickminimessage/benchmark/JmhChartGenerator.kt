package gg.aquatic.quickminimessage.benchmark

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.CategoryChart
import org.knowm.xchart.CategoryChartBuilder
import java.io.File

object JmhChartGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 2) {
            System.err.println("Usage: JmhChartGenerator <results.json> <output-dir>")
            return
        }

        val resultsFile = File(args[0])
        if (!resultsFile.isFile) {
            System.err.println("JMH results file not found: ${resultsFile.absolutePath}")
            return
        }

        val outputDir = File(args[1])
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Unable to create output directory: ${outputDir.absolutePath}")
            return
        }

        val mapper = ObjectMapper()
        val root = mapper.readTree(resultsFile)
        if (!root.isArray || root.size() == 0) {
            System.err.println("No JMH results found in: ${resultsFile.absolutePath}")
            return
        }

        val entries = root.mapNotNull { node -> parseEntry(node) }
        if (entries.isEmpty()) {
            System.err.println("No usable JMH results found in: ${resultsFile.absolutePath}")
            return
        }

        val grouped = entries.groupBy { it.className }
        for ((className, classEntries) in grouped) {
            val chart = buildChart(className, classEntries)
            val outputFile = File(outputDir, "${className}.png")
            BitmapEncoder.saveBitmap(chart, outputFile.absolutePath, BitmapEncoder.BitmapFormat.PNG)
            println("Wrote ${outputFile.absolutePath}")
        }
    }

    private fun parseEntry(node: JsonNode): ResultEntry? {
        val benchmark = node.path("benchmark").asText(null) ?: return null
        val metric = node.path("primaryMetric")
        val score = metric.path("score").asDouble(Double.NaN)
        if (score.isNaN()) {
            return null
        }
        val scoreError = metric.path("scoreError").asDouble(0.0)
        val scoreUnit = metric.path("scoreUnit").asText("score")
        val params = node.path("params")
        val scenario = if (!params.isMissingNode) {
            params.path("scenario").asText(params.path("input").asText("unknown"))
        } else {
            "unknown"
        }

        val methodName = benchmark.substringAfterLast('.')
        val className = benchmark.substringBeforeLast('.').substringAfterLast('.').lowercase()
        val series = when {
            methodName.contains("QuickMiniMessage") -> "QuickMiniMessage"
            methodName.contains("MiniMessage") -> "MiniMessage"
            else -> methodName
        }

        return ResultEntry(className, series, scenario, score, scoreError, scoreUnit)
    }

    private fun buildChart(className: String, entries: List<ResultEntry>): CategoryChart {
        val scoreUnit = entries.first().scoreUnit
        val order = scenarioOrder(className, entries)
        val scenarios = order.ifEmpty { entries.map { it.scenario }.distinct().sorted() }

        val chart = CategoryChartBuilder()
            .width(1200)
            .height(600)
            .title(chartTitle(className))
            .xAxisTitle("Scenario")
            .yAxisTitle(scoreUnit)
            .build()

        chart.styler.xAxisLabelRotation = 20
        chart.styler.availableSpaceFill = 0.9
        chart.styler.isLegendVisible = true

        val bySeries = entries.groupBy { it.series }
        for ((series, seriesEntries) in bySeries) {
            val scores = scenarios.map { scenario ->
                seriesEntries.firstOrNull { it.scenario == scenario }?.score ?: 0.0
            }
            val errors = scenarios.map { scenario ->
                seriesEntries.firstOrNull { it.scenario == scenario }?.scoreError ?: 0.0
            }
            chart.addSeries(series, scenarios, scores, errors)
        }

        return chart
    }

    private fun scenarioOrder(className: String, entries: List<ResultEntry>): List<String> {
        return when (className) {
            "mmparserbenchmark" -> listOf(
                "plain",
                "simple",
                "nested",
                "gradient",
                "rainbow",
                "hover",
                "nbt",
                "translatable"
            )
            "mmparserresolverbenchmark" -> listOf(
                "parsed",
                "unparsed",
                "styling",
                "number",
                "joining"
            )
            else -> entries.map { it.scenario }.distinct()
        }
    }

    private fun chartTitle(className: String): String {
        return when (className) {
            "mmparserbenchmark" -> "Parse Benchmark (QuickMiniMessage vs MiniMessage)"
            "mmparserresolverbenchmark" -> "Resolver Benchmark (QuickMiniMessage vs MiniMessage)"
            else -> "Benchmark: $className"
        }
    }

    private data class ResultEntry(
        val className: String,
        val series: String,
        val scenario: String,
        val score: Double,
        val scoreError: Double,
        val scoreUnit: String
    )
}
