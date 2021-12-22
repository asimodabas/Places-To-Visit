package com.asimodabas.places_to_visit.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.asimodabas.places_to_visit.model.Place

@Database(entities = [Place::class], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

}
