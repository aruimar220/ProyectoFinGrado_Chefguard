package com.example.chefguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefguard.model.AlimentoEntity
import com.example.chefguard.model.AppDatabase
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
                db.alimentoDao().insertarAlimento(alimento)
            } else {
                db.alimentoDao().actualizarAlimento(alimento)
            }
        }
    }
}