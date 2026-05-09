package com.example.pekomon.weatherly.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WeatherlyWidgetUpdater {
    suspend fun refresh(context: Context) {
        WeatherlyCurrentWeatherWidget().updateAll(context.applicationContext)
    }
}
