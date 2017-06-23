package com.elpassion.crweather

import android.content.Context
import android.util.AttributeSet
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay


class ChartView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : CrView(context, attrs, defStyleAttr, defStyleRes) {

    var chart: Chart = Chart(0f..100f, 0f..100f, emptyList())
        set(value) {
            field = value
            actor.offer(value)
        }

    val actor = actor<Chart>(UI, Channel.CONFLATED) {

        var currentChart = chart.deepCopy()
        var currentVelocities = chart.deepCopy().resetPoints()

        doOnDraw { drawChart(currentChart) }

        for (destinationChart in this) {

            currentChart = currentChart.copyAndReformat(destinationChart, defaultPoint = destinationChart.pointAtTheEnd)
            currentVelocities = currentVelocities.copyAndReformat(destinationChart, defaultPoint = Point(0f, 0f))
            while (isActive && isEmpty) {
                redraw()
                delay(10)
                currentChart.moveABitTo(destinationChart, currentVelocities)
            }
        }
    }
}