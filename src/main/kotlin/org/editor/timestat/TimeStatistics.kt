package org.editor.timestat

class TimeStatistics {
    private val times = ArrayDeque<Long>()
    private val coefficients = List(10) { (100 - it * it).toLong() }
    private var expectedTime: Long = 10000
    private var currentTime: Long = 0

    private fun calculateExpectedTime() {
        expectedTime =
            times.foldIndexed(0L) { index, acc, l -> acc + l * coefficients[index] } / coefficients.take(times.size)
                .sum()
    }

    fun add(time: Long) {
        while (times.size >= 10) {
            times.removeLast()
        }
        times.addFirst(time)
        calculateExpectedTime()
    }

    fun startExecution() {
        currentTime = System.currentTimeMillis()
    }

    fun endExecution() {
        val c = System.currentTimeMillis()
        add(c - currentTime)
    }

    fun getExpectedTime(): Long {
        return expectedTime
    }
}