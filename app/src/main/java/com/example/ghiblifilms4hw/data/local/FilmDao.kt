package com.example.ghiblifilms4hw.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ghiblifilms4hw.model.Film
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmDao {
    @Query("SELECT * FROM films")
    fun getAllFilms(): Flow<List<Film>>

    @Query("SELECT * FROM films WHERE id = :id")
    suspend fun getFilmById(id: String): Film?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilms(films: List<Film>)

    @Query("UPDATE films SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

}