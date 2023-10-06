package com.example.flightsearchapp

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flightsearchapp.data.AppDatabase
import com.example.flightsearchapp.data.UserPreferencesRepository

private const val SEACRH_TEXT_PREFERENCE = "search_text"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SEACRH_TEXT_PREFERENCE
)

class FlightSearchApplication: Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}