package data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.local.dao.AlertaDao
import data.local.entity.AlertaEntity
import data.local.dao.AlimentoDao
import data.local.entity.AlimentoEntity
import data.local.dao.UsuarioDao
import data.local.entity.UsuarioEntity

@Database(
    entities = [UsuarioEntity::class, AlimentoEntity::class, AlertaEntity::class],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun alimentoDao(): AlimentoDao
    abstract fun alertaDao(): AlertaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chefguard_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}