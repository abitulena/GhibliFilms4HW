package com.example.ghiblifilms4hw.data.remote

import com.google.gson.annotations.SerializedName

data class FilmDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val director: String? = null,
    val producer: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("rt_score")
    val rtScore: String? = null,
    val image: String? = null,
    @SerializedName("movie_banner")
    val movieBanner: String? = null
)