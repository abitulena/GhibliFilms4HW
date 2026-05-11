package com.example.ghiblifilms4hw.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ghiblifilms4hw.model.Film

@Database(
    entities = [Film::class],
    version = 5,
    exportSchema = false
)
abstract class FilmDatabase : RoomDatabase() {
    abstract fun filmDao(): FilmDao
}