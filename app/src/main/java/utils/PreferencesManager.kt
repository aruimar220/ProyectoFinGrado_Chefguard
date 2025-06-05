package com.example.chefguard.utils

import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "ChefGuardPrefs" // Nombre de las preferencias compartidas para ChefGuard
    private const val KEY_IS_LOGGED_IN = "isLoggedIn" // Clave para almacenar el estado de inicio de sesión en las preferencias compartidas
    private const val KEY_USER_ID = "userId" // Clave para almacenar el ID del usuario en las preferencias compartidas
    private const val KEY_REMEMBER_ME = "rememberMe" // Clave para almacenar el valor de recordar al iniciar sesión en las preferencias compartidas

    fun saveLoginState(context: Context, isLoggedIn: Boolean) { // Función para guardar el estado de inicio de sesión en las preferencias compartidas
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Obtener las preferencias compartidas para ChefGuard con el nombre especificado
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply() // Guardar el estado de inicio de sesión en las preferencias compartidas con la clave correspondiente
    }

    fun saveUserId(context: Context, userId: Int) { // Función para guardar el ID del usuario en las preferencias compartidas con la clave correspondiente para ChefGuard
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Obtener las preferencias compartidas para ChefGuard con el nombre especificado y en modo privado
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply() // Guardar el ID del usuario en las preferencias compartidas con la clave correspondiente
    }

    fun getUserId(context: Context): Int { // Función para obtener el ID del usuario desde las preferencias compartidas
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Obtener las preferencias compartidas para ChefGuard con el nombre especificado
        return sharedPreferences.getInt(KEY_USER_ID, -1) // Obtener el ID del usuario de las preferencias compartidas con la clave correspondiente, o -1 si no se encuentra
    }

    fun clearUserId(context: Context) { // Función para eliminar el ID del usuario de las preferencias compartidas con la clave correspondiente para ChefGuard
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Obtener las preferencias compartidas para ChefGuard con el nombre especificado y en modo privado
        sharedPreferences.edit().remove(KEY_USER_ID).apply() // Eliminar el ID del usuario de las preferencias compartidas con la clave correspondiente
    }

    fun saveRememberMe(context: Context, rememberMe: Boolean) { // Función para guardar el valor de recordar al iniciar sesión en las preferencias compartidas
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Obtener las preferencias compartidas para ChefGuard con el nombre especificado y en modo privado
        prefs.edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply() // Guardar el valor de recordar al iniciar sesión en las preferencias compartidas con la clave correspondiente
    }

    fun getRememberMe(context: Context): Boolean { // Función para obtener el valor de recordar al iniciar sesión desde las preferencias compartidas
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Obtener las preferencias compartidas para ChefGuard con el nombre especificado y en modo privado
        return prefs.getBoolean(KEY_REMEMBER_ME, false) // Obtener el valor de recordar al iniciar sesión de las preferencias compartidas con la clave correspondiente, o false si no se encuentra
    }

    fun getLoginState(context: Context): Boolean { // Función para obtener el estado de inicio de sesión desde las preferencias compartidas
        val userId = getUserId(context) // Obtener el ID del usuario desde las preferencias compartidas
        val rememberMe = getRememberMe(context) // Obtener el valor de recordar al iniciar sesión desde las preferencias compartidas
        return userId != -1 && rememberMe // Devolver true si el ID del usuario y el valor de recordar al iniciar sesión son válidos
    }

}