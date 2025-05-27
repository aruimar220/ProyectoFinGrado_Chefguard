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

class AlimentoViewModel : ViewModel() {
    private val _alimento = MutableStateFlow<AlimentoEntity?>(null)
    val alimento: StateFlow<AlimentoEntity?> = _alimento

    fun cargarAlimento(db: AppDatabase, id: Int) {
        viewModelScope.launch {
            _alimento.value = db.alimentoDao().obtenerAlimentoPorId(id)
        }
    }

    fun guardarAlimento(db: AppDatabase, alimento: AlimentoEntity) {
        viewModelScope.launch {
            if (alimento.id == 0) {
                val newId = db.alimentoDao().insertarAlimento(alimento).toInt()
                val alimentoConId = alimento.copy(id = newId)
                FirestoreSyncHelper.syncAlimento(alimentoConId)
            } else {
                db.alimentoDao().actualizarAlimento(alimento)
                FirestoreSyncHelper.syncAlimento(alimento)
            }
        }
    }
    fun eliminarAlimento(db: AppDatabase, alimentoId: Int) {
        viewModelScope.launch {
            db.alimentoDao().eliminarAlimento(alimentoId)
            FirestoreSyncHelper.eliminarAlimentoDeFirestore(alimentoId)
        }
    }

    fun sincronizarConFirestore(context: Context, usuarioId: Int) {
        FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(context, usuarioId)
    }

}