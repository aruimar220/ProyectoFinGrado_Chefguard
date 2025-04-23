package com.example.chefguard.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlimentoEntity::class, UsuarioEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alimentoDao(): AlimentoDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chefguard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}