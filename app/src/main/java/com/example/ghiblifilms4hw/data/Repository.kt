package com.example.ghiblifilms4hw.data

import com.example.ghiblifilms4hw.data.local.FilmDao
import com.example.ghiblifilms4hw.data.remote.GhibliApiService
import com.example.ghiblifilms4hw.model.Film
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val api: GhibliApiService,
    private val filmDao: FilmDao
) {

    fun getAllFilms(): Flow<List<Film>> = filmDao.getAllFilms()

    suspend fun getFilmById(id: String): Film? = filmDao.getFilmById(id)

    suspend fun refreshFilms(): Result<Unit> {
        return try {
            val filmsFromApi = api.getFilms()
            val existingFavorites = filmDao.getAllFilms().first().associateBy { it.id }

            val filmsToInsert = filmsFromApi.map { filmFromApi ->
                filmFromApi.copy(
                    isFavorite = existingFavorites[filmFromApi.id]?.isFavorite ?: false
                )
            }
            filmDao.insertFilms(filmsToInsert)
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun toggleFavorite(filmId: String): Result<Unit> {
        return try {
            val film = filmDao.getFilmById(filmId)
            if (film != null) {
                filmDao.updateFavoriteStatus(filmId, !film.isFavorite)
            }
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getFilmFromApiById(id: String): Film? {
        return try {
            api.getFilmById(id)
        } catch (_: Exception) {
            null
        }
    }
}