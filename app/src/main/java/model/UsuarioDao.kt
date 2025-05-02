package com.example.chefguard.model

import androidx.room.*

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun validarUsuario(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios WHERE id = :userId")
    suspend fun eliminarUsuarioPorId(userId: Int)

    @Query("UPDATE usuarios SET contrasena = :nuevaContrasena WHERE id = :userId")
    suspend fun actualizarContraseña(userId: Int, nuevaContrasena: String)
}