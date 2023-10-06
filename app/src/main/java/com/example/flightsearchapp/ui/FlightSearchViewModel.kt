package com.example.flightsearchapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key.Companion.F
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearchapp.FlightSearchApplication
import com.example.flightsearchapp.data.Airport
import com.example.flightsearchapp.data.Favorite
import com.example.flightsearchapp.data.FlightSearchDao
import com.example.flightsearchapp.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlightSearchViewModel(
    private val flightSearchDao: FlightSearchDao,
    private val userPreferencesRepository: UserPreferencesRepository
    ): ViewModel() {

    /*
    val uiState: StateFlow<String> = userPreferencesRepository.searchText.filter {
        it.isNotEmpty()
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

     */

    var userSearch by mutableStateOf("")
        private set

    var chosenAirport by mutableStateOf(Airport(0,"","",0))
        private set

    var favList: MutableList<Pair<Airport, Airport>>  = mutableListOf()
        private set


    fun updateUserSearch(searchWord: String){
        /*
        viewModelScope.launch {
            userPreferencesRepository.saveSearchText(searchWord)
        }
        */
        userSearch = searchWord
    }

    fun updateChosenAirport(airport: Airport){
        chosenAirport = airport
    }

    fun autoComplete(): Flow<List<Airport>> {
        if (userSearch == "") {
            return emptyFlow()
        } else {
            return flightSearchDao.autoComplete(userSearch)
        }
    }

    fun getAllFlights(): Flow<List<Airport>> = flightSearchDao.getAllFlights(chosenAirport.id)

    suspend fun saveFavorite(departure_code: String, destination_code: String) = flightSearchDao.insert(departure_code, destination_code)

    suspend fun deleteFavorite(departure_code: String, destination_code: String) = flightSearchDao.delete(departure_code, destination_code)

    fun getFavoriteList(): Flow<List<Favorite>> = flightSearchDao.getFavoriteList()

    fun flightPairUpList(favoriteList: List<Favorite>) {
        favList.clear()
        favoriteList.forEach {favorite ->
            var wait = viewModelScope.launch(Dispatchers.Default) {
                favList.add(
                    Pair(
                        flightSearchDao.getFlight(favorite.departureCode),
                        flightSearchDao.getFlight(favorite.destinationCode)
                    )
                )
            }
            while(!wait.isCompleted) {}
        }
    }

    fun checkFavorite(departure_code: String, destination_code: String) : Flow<Boolean> = flightSearchDao.checkFavorite(departure_code,destination_code)

    companion object {
        val factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightSearchViewModel(application.database.flightSearchDao(), application.userPreferencesRepository)
            }
        }
    }
}