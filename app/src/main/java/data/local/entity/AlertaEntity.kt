package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alertas")
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ID_usuario: Int,
    val mensaje: String,
    val fecha: String,
    val tipo: String
)