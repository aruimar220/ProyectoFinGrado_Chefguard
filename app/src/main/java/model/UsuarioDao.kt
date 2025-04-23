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
}