package com.example.ghiblifilms4hw.data

import com.example.ghiblifilms4hw.data.local.FilmDao
import com.example.ghiblifilms4hw.data.remote.FilmDto
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

    fun getFavoriteFilms(): Flow<List<Film>> = filmDao.getFavoriteFilms()

    suspend fun getFilmById(id: String): Film? = filmDao.getFilmById(id)

    suspend fun refreshFilms(): Result<Unit> {
        return try {
            val filmsFromApi = api.getFilms()
            val existingFavorites = filmDao.getAllFilms().first().associateBy { it.id }

            val filmsToInsert = filmsFromApi.map { dto ->
                Film(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    director = dto.director,
                    producer = dto.producer,
                    releaseDate = dto.releaseDate,
                    rtScore = dto.rtScore,
                    image = dto.image,
                    isFavorite = existingFavorites[dto.id]?.isFavorite ?: false
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

    suspend fun saveFilmToCache(filmDto: FilmDto): Result<Unit> {
        return try {
            val existingFilm = filmDao.getFilmById(filmDto.id)
            val film = Film(
                id = filmDto.id,
                title = filmDto.title,
                description = filmDto.description,
                director = filmDto.director,
                producer = filmDto.producer,
                releaseDate = filmDto.releaseDate,
                rtScore = filmDto.rtScore,
                image = filmDto.image,
                isFavorite = existingFilm?.isFavorite ?: false
            )
            filmDao.insertFilm(film)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFilmFromApiById(id: String): FilmDto? {
        return try {
            api.getFilmById(id)
        } catch (_: Exception) {
            null
        }
    }
}
