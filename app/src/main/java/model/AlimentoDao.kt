package com.example.chefguard.model

import androidx.room.*

@Dao
interface AlimentoDao {
    @Insert
    suspend fun insertarAlimento(alimento: AlimentoEntity)

    @Query("SELECT * FROM alimentos")
    suspend fun obtenerTodosLosAlimentos(): List<AlimentoEntity>

    @Query("SELECT * FROM alimentos WHERE id = :id")
    suspend fun obtenerAlimentoPorId(id: Int): AlimentoEntity?
}