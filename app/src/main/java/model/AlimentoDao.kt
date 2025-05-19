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

    @Query("SELECT * FROM alimentos WHERE ID_usuario = :userId")
    suspend fun obtenerAlimentosPorUsuario(userId: Int): List<AlimentoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlimentos(alimentos: List<AlimentoEntity>)

    @Query("SELECT * FROM alimentos WHERE nombre = :nombre AND lote = :lote AND proveedor = :proveedor AND ID_usuario = :usuarioId LIMIT 1")
    fun buscarPorNombreLoteProveedor(nombre: String, lote: String, proveedor: String, usuarioId: Int): AlimentoEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAlimentosSinDuplicados(alimentos: List<AlimentoEntity>)
}