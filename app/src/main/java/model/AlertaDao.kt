package com.example.chefguard.model

import androidx.room.*

@Dao
interface AlertaDao {
    @Insert
    suspend fun insertarAlerta(alerta: AlertaEntity)

    @Query("SELECT * FROM alertas WHERE ID_usuario = :userId")
    suspend fun obtenerAlertasPorUsuario(userId: Int): List<AlertaEntity>
}