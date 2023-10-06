package com.example.flightsearchapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearchapp.R
import com.example.flightsearchapp.data.Airport
import kotlinx.coroutines.launch

@Composable
fun FlightSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: FlightSearchViewModel = viewModel(factory = FlightSearchViewModel.factory),
) {
    val searchText = viewModel.userSearch
    val chosenAirport = viewModel.chosenAirport
    val autoCompleteTexts by viewModel.autoComplete().collectAsState(emptyList())
    val flights by viewModel.getAllFlights().collectAsState(emptyList())
    val favorites by viewModel.getFavoriteList().collectAsState(emptyList())
    var autoCompleteShow by remember{ mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                modifier = modifier
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues)
        ) {
            SearchBar(
                searchText = searchText,
                onValueChange = {
                    viewModel.updateUserSearch(it)
                    autoCompleteShow = true
                                },
                modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            )
            Box(
                modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Column {
                    Text(
                        text = if (searchText == "") {
                            stringResource(R.string.favourite_routes)
                        } else {
                            stringResource(R.string.flights_from, chosenAirport.iataCode)
                               },
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.Top
                    ) {
                        if (searchText == "") {
                            viewModel.flightPairUpList(favorites)
                            items(items = viewModel.favList) { favorite ->
                                val faved by viewModel.checkFavorite(favorite.first.iataCode, favorite.second.iataCode).collectAsState(true)
                                FlightItem(
                                    depart = favorite.first,
                                    arrive = favorite.second,
                                    saved = faved,
                                    star = { depart, arrive ->
                                        coroutineScope.launch {
                                            if (faved) {
                                                viewModel.deleteFavorite(
                                                    depart.iataCode,
                                                    arrive.iataCode
                                                )
                                            } else {
                                                viewModel.saveFavorite(
                                                    depart.iataCode,
                                                    arrive.iataCode
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            items(items = flights) { flight ->
                                val faved by viewModel.checkFavorite(chosenAirport.iataCode,flight.iataCode).collectAsState(false)
                                FlightItem(
                                    depart = chosenAirport,
                                    arrive = flight,
                                    saved = faved,
                                    star = { depart, arrive ->
                                        coroutineScope.launch {
                                            if (faved) {
                                                viewModel.deleteFavorite(
                                                    depart.iataCode,
                                                    arrive.iataCode
                                                )
                                            } else {
                                                viewModel.saveFavorite(
                                                    depart.iataCode,
                                                    arrive.iataCode
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                if (searchText != "" && autoCompleteShow) {
                    if (autoCompleteTexts != emptyList<Airport>()) {
                        viewModel.updateChosenAirport(autoCompleteTexts[0])
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.Top,
                        modifier = modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        items(items = autoCompleteTexts) { autoCompleteText ->
                            FlightSearchItem(
                                autoCompleteText = autoCompleteText,
                                updateChosenAirport = {
                                    viewModel.updateChosenAirport(it)
                                    autoCompleteShow = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextField(
            value = searchText,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.baseline_search_24),
                    contentDescription = stringResource(R.string.searchicon)
                )
            },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.baseline_mic_24),
                    contentDescription = stringResource(R.string.micicon)
                )
            },
            singleLine = true,
            shape = shapes.large,
            modifier = modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    }
}

@Composable
fun FlightSearchItem(
    autoCompleteText: Airport,
    modifier: Modifier = Modifier,
    updateChosenAirport: (Airport) -> Unit
) {
    Card(
        colors = cardColors(MaterialTheme.colorScheme.background),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = { updateChosenAirport(autoCompleteText) }
    ) {
        Row {
            Text(
                text = autoCompleteText.iataCode,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(end = 4.dp)
            )
            Text(text = autoCompleteText.name)
        }
    }
}

@Composable
fun FlightItem(
    depart: Airport,
    arrive: Airport,
    saved: Boolean,
    star: (Airport, Airport) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.depart),
                    style = MaterialTheme.typography.labelSmall
                )
                Row {
                    Text(
                        text = depart.iataCode,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = depart.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = stringResource(R.string.arrive),
                    style = MaterialTheme.typography.labelSmall
                )
                Row {
                    Text(
                        text = arrive.iataCode,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = arrive.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }//end column
            IconButton(
                onClick = {
                    star(depart, arrive)
                          },
                modifier = modifier
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    modifier = modifier
                        .size(36.dp),
                    tint = if (saved) Color(0xFFFFFF00) else Color(0xFF000000),
                    painter = painterResource(R.drawable.baseline_star_24),
                    contentDescription = stringResource(R.string.staricon)
                )
            }
        }
    }
}


