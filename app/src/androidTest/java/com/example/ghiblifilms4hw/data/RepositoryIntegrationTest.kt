package com.example.ghiblifilms4hw.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ghiblifilms4hw.data.local.FilmDatabase
import com.example.ghiblifilms4hw.data.remote.FilmDto
import com.example.ghiblifilms4hw.data.remote.GhibliApiService
import com.example.ghiblifilms4hw.model.FilmEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {

    @MockK
    private lateinit var api: GhibliApiService

    private lateinit var database: FilmDatabase
    private lateinit var repository: Repository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FilmDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = Repository(api, database.filmDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshFilmsSyncsApiDataToDatabaseCorrectly() = runTest {
        val apiFilms = listOf(
            FilmDto("1", "Spirited Away", director = "Miyazaki"),
            FilmDto("2", "Totoro", director = "Miyazaki")
        )
        coEvery { api.getFilms() } returns apiFilms

        val result = repository.refreshFilms()
        val filmsInDb = repository.getAllFilms().first()

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(2, filmsInDb.size)
    }

    @Test
    fun favoritesPersistAfterRefresh() = runTest {
        val favoriteFilm = FilmEntity("1", "Spirited Away", isFavorite = true)
        database.filmDao().insertFilm(favoriteFilm)

        val apiFilms = listOf(FilmDto("1", "Spirited Away"))
        coEvery { api.getFilms() } returns apiFilms

        repository.refreshFilms()
        val films = repository.getAllFilms().first()

        Assert.assertTrue(films[0].isFavorite)
    }
}