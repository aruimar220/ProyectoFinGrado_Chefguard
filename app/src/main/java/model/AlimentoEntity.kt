package com.example.chefguard.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alimentos")
data class AlimentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ID_usuario: Int,
    val nombre: String,
    val cantidad: Int,
    val fechaCaducidad: String?,
    val fechaConsumo: String?,
    val lote: String?,
    val estado: String?,
    val proveedor: String?,
    val tipoAlimento: String?,
    val ambiente: String?
)