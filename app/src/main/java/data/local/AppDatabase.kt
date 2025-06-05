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
// Se declaran las entidades y el DAO de la base de datos, junto a la versión
@Database(
    entities = [UsuarioEntity::class, AlimentoEntity::class, AlertaEntity::class],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao //DAO de usuario
    abstract fun alimentoDao(): AlimentoDao //DAO de alimento
    abstract fun alertaDao(): AlertaDao //DAO de alerta

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null //Instancia unica de la base de datos
        // Si la base de datos no existe, se crea una nueva instancia
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // Comprobamos primero si ya existe la instancia
                val instance = Room.databaseBuilder(// Si aún no existe, la construimos
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