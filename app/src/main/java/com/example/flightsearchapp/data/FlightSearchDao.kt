package com.example.flightsearchapp.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightSearchDao {
    @Query("""
        SELECT * FROM airport 
        WHERE iata_code LIKE '%' || :userSearch || '%' OR name LIKE '%' ||  :userSearch || '%'
        ORDER BY passengers DESC;
        """)
    fun autoComplete(userSearch: String): Flow<List<Airport>>

    @Query("""
        SELECT * FROM airport
        WHERE id != :chosenAirportId
        ORDER BY passengers DESC;
        """)
    fun getAllFlights(chosenAirportId: Int): Flow<List<Airport>>

    @Query("""
        SELECT * FROM favorite;
        """)
    fun getFavoriteList(): Flow<List<Favorite>>

    @Query("""
        SELECT * FROM airport
        WHERE iata_code = :iata_code;
        """)
    fun getFlight(iata_code: String): Airport

    @Query("""
        INSERT INTO favorite
        VALUES ( NULL, :departure_code, :destination_code);
        """)
    suspend fun insert(departure_code: String, destination_code: String)

    @Query("""
        DELETE FROM favorite
        WHERE departure_code = :departure_code AND destination_code = :destination_code;
        """)
    suspend fun delete(departure_code: String, destination_code: String)

    @Query("""
        SELECT EXISTS(
            SELECT * FROM favorite
            WHERE departure_code = :departure_code AND destination_code = :destination_code
        )
        """)
    fun checkFavorite(departure_code: String, destination_code: String): Flow<Boolean>



}