package com.example.chefguard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.data.remote.FirestoreSyncHelper
import data.local.entity.AlimentoEntity
import data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
// Clase alimento viewmodel
class AlimentoViewModel : ViewModel() {
    private val _alimento = MutableStateFlow<AlimentoEntity?>(null) // Estado mutable para el alimento seleccionado en la lista
    val alimento: StateFlow<AlimentoEntity?> = _alimento // Estado inmutable para el alimento seleccionado en la lista

    // Función para cargar un alimento por su ID desde la base de datos local
    fun cargarAlimento(db: AppDatabase, id: Int) {
        viewModelScope.launch {
            _alimento.value = db.alimentoDao().obtenerAlimentoPorId(id) // Cargar el alimento desde la base de datos local y actualizar el estado mutable
        }
    }
    // Función para guardar un alimento en la base de datos local y en Firestore si es necesario
    fun guardarAlimento(db: AppDatabase, alimento: AlimentoEntity) {
        viewModelScope.launch {
            if (alimento.id == 0) { // Si el ID es 0, insertar el alimento en la base de datos local y obtener su ID generado automáticamente
                val newId = db.alimentoDao().insertarAlimento(alimento).toInt() // Insertar el alimento en la base de datos local y obtener su ID generado automáticamente
                val alimentoConId = alimento.copy(id = newId) // Crea un nuevo objeto AlimentoEntity con el ID generado automáticamente
                FirestoreSyncHelper.syncAlimento(alimentoConId) // Sube el alimento a Firestore con el ID generado automáticamente
            } else { // Si el ID no es 0, actualizar el alimento en la base de datos local y subirlo a Firestore
                db.alimentoDao().actualizarAlimento(alimento) // Actualiza el alimento en la base de datos local
                FirestoreSyncHelper.syncAlimento(alimento) // Sube el alimento a Firestore
            }
        }
    }
    // Función para eliminar un alimento de la base de datos local y de Firestore
    fun eliminarAlimento(db: AppDatabase, alimentoId: Int) {
        viewModelScope.launch {
            db.alimentoDao().eliminarAlimento(alimentoId) // Elimina el alimento de la base de datos local con el ID proporcionado
            FirestoreSyncHelper.eliminarAlimentoDeFirestore(alimentoId) // Elimina el alimento de Firestore con el ID proporcionado
        }
    }
    // Función para sincronizar alimentos desde Firestore a la base de datos local y viceversa
    fun sincronizarConFirestore(context: Context) {
        viewModelScope.launch {
            FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(context) // Sincroniza alimentos desde Firestore a la base de datos local y viceversa
        }
    }
}