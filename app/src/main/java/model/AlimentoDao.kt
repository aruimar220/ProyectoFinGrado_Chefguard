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

    @Query("DELETE FROM alimentos WHERE id = :id")
    suspend fun eliminarAlimento(id: Int)

    @Update
    suspend fun actualizarAlimento(alimento: AlimentoEntity)
}