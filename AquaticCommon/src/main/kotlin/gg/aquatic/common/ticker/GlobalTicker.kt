package gg.aquatic.common.ticker

import gg.aquatic.common.coroutine.SingleThreadedContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

object GlobalTicker {

    private val tickers = ArrayList<Ticker>(HashSet())
    private val scheduler = SingleThreadedContext("GlobalTicker")

    private lateinit var tickerJob: Job

    internal fun initialize() {
        tickerJob = startTicker {
            for (ticker in tickers) {
                ticker.onTick()
            }
        }
    }

    private fun startTicker(
        periodMs: Long = 50L,
        onTick: suspend () -> Unit
    ): Job {
        return scheduler.scope.launch {
            while (isActive) {
                onTick()
                delay(periodMs)
            }
        }
    }

    fun register(ticker: Ticker) {
        synchronized(tickers) {
            tickers.add(ticker)
        }
    }
    fun unregister(ticker: Ticker) {
        synchronized(tickers) {
            tickers.remove(ticker)
        }
    }
}