package com.example.ghiblifilms4hw.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "films")
data class Film(
    @PrimaryKey
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
    val isFavorite: Boolean = false
) : Parcelable