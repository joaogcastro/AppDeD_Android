package com.example.appded.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appded.player.PlayerEntity

@Database(entities = [PlayerEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
