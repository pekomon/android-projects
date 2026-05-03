package com.example.pekomon.weatherly.data.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.weatherlyDataStore by preferencesDataStore(name = "weatherly_preferences")
