package com.example.pekomon.weatherly.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.pekomon.weatherly.MainActivity
import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.WeatherCondition
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.core.ui.formatTemperature
import com.example.pekomon.weatherly.core.ui.formatWindSpeed
import com.example.pekomon.weatherly.data.repository.DataStoreSettingsRepository
import com.example.pekomon.weatherly.data.repository.OpenMeteoWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherlyCurrentWeatherWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val appContext = context.applicationContext
        val settings = DataStoreSettingsRepository(appContext).settings.value
        val weatherResult = withContext(Dispatchers.IO) {
            runCatching {
                OpenMeteoWeatherRepository().getCurrentLocationWeather()
            }
        }

        provideContent {
            WeatherlyCurrentWeatherWidgetContent(
                weatherDetails = weatherResult.getOrNull(),
                settings = settings,
                errorMessage = weatherResult.exceptionOrNull()?.message,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun WeatherlyCurrentWeatherWidgetContent(
    weatherDetails: WeatherDetails?,
    settings: AppSettings,
    errorMessage: String?,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(ColorProvider(Color(0xFF1B2B40)))
            .padding(18.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.TopStart,
    ) {
        if (weatherDetails == null) {
            ErrorContent(errorMessage = errorMessage)
        } else {
            WidgetWeatherContent(
                weatherDetails = weatherDetails,
                settings = settings,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetWeatherContent(
    weatherDetails: WeatherDetails,
    settings: AppSettings,
) {
    val location = weatherDetails.location
    val current = weatherDetails.currentWeather
    val today = weatherDetails.dailyForecast.firstOrNull()

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = "Weatherly",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB5C8E0)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(10.dp))
        Text(
            text = location.name,
            style = TextStyle(
                color = ColorProvider(Color(0xFFF2F6FB)),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = formatTemperature(current.temperature, settings, includeUnit = true),
            style = TextStyle(
                color = ColorProvider(Color(0xFFF2F6FB)),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = conditionLabel(current.condition),
            style = TextStyle(
                color = ColorProvider(Color(0xFFB5C8E0)),
            ),
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "Feels like ${formatTemperature(current.apparentTemperature, settings)}",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB5C8E0)),
            ),
        )
        Text(
            text = "Wind ${formatWindSpeed(current.windSpeed, settings)}",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB5C8E0)),
            ),
        )
        today?.let { forecast ->
            Spacer(modifier = GlanceModifier.height(12.dp))
            Text(
                text = "Today ${formatTemperature(forecast.minTemperature, settings)} / ${formatTemperature(forecast.maxTemperature, settings)}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFF2F6FB)),
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun ErrorContent(errorMessage: String?) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = "Weatherly",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB5C8E0)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "Widget unavailable",
            style = TextStyle(
                color = ColorProvider(Color(0xFFF2F6FB)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = errorMessage ?: "Tap to open the app and refresh weather.",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB5C8E0)),
            ),
        )
    }
}

private fun conditionLabel(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.Clear -> "Clear sky"
    WeatherCondition.MostlyClear -> "Mostly clear"
    WeatherCondition.PartlyCloudy -> "Partly cloudy"
    WeatherCondition.Cloudy -> "Cloudy"
    WeatherCondition.Fog -> "Foggy"
    WeatherCondition.Drizzle -> "Drizzle"
    WeatherCondition.Rain -> "Rain"
    WeatherCondition.Snow -> "Snow"
    WeatherCondition.Thunderstorm -> "Thunderstorm"
    WeatherCondition.Windy -> "Windy"
    WeatherCondition.Unknown -> "Unknown conditions"
}

class WeatherlyCurrentWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherlyCurrentWeatherWidget()
}
