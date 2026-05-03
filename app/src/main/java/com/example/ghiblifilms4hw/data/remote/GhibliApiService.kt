package com.example.ghiblifilms4hw.data.remote

import com.example.ghiblifilms4hw.model.Film
import retrofit2.http.GET
import retrofit2.http.Path

interface GhibliApiService {
    @GET("films")
    suspend fun getFilms(): List<Film>

    @GET("films/{id}")
    suspend fun getFilmById(@Path("id") id: String): Film
}