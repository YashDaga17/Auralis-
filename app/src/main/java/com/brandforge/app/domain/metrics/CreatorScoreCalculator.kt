package com.brandforge.app.domain.metrics

import com.brandforge.app.core.model.CreatorMetric
import kotlin.math.roundToInt
import javax.inject.Inject

class CreatorScoreCalculator @Inject constructor() {
    fun calculate(metrics: List<CreatorMetric>): Int {
        if (metrics.isEmpty()) return 0
        val weighted = metrics.mapIndexed { index, metric ->
            val weight = when (index) {
                0 -> 1.25f
                1 -> 1.15f
                2 -> 1.1f
                else -> 1f
            }
            metric.progress.coerceIn(0f, 1f) * weight
        }
        val maxWeight = metrics.indices.sumOf { index ->
            when (index) {
                0 -> 1.25
                1 -> 1.15
                2 -> 1.1
                else -> 1.0
            }
        }
        return ((weighted.sum() * 100) / maxWeight).roundToInt().coerceIn(0, 100)
    }
}
