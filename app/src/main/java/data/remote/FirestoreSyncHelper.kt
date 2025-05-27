package com.tuapp.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import data.local.AppDatabase
import data.local.entity.AlertaEntity
import data.local.entity.AlimentoEntity
import data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirestoreSyncHelper {

    private val firestore = FirebaseFirestore.getInstance()

    fun syncAlimento(alimento: AlimentoEntity) {
        val db = FirebaseFirestore.getInstance()
        db.collection("alimentos")
            .document(alimento.id.toString())
            .set(
                mapOf(
                    "nombre" to alimento.nombre,
                    "cantidad" to alimento.cantidad,
                    "fechaCaducidad" to alimento.fechaCaducidad,
                    "fechaConsumo" to alimento.fechaConsumo,
                    "lote" to alimento.lote,
                    "estado" to alimento.estado,
                    "proveedor" to alimento.proveedor,
                    "tipoAlimento" to alimento.tipoAlimento,
                    "ambiente" to alimento.ambiente,
                    "ID_usuario" to alimento.ID_usuario
                )
            )
            .addOnSuccessListener { println("Alimento sincronizado con Firestore") }
            .addOnFailureListener { e -> println("Error al sincronizar: ${e.message}") }
    }

    fun sincronizarAlimentosDesdeFirestore(context: Context, usuarioId: Int) {
        val firestore = FirebaseFirestore.getInstance()
        val db = AppDatabase.getDatabase(context)
        val alimentoDao = db.alimentoDao()

        firestore.collection("alimentos")
            .whereEqualTo("ID_usuario", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombre") ?: continue
                    val cantidad = document.getDouble("cantidad")?.toInt() ?: continue
                    val fechaCaducidad = document.getString("fechaCaducidad") ?: continue
                    val fechaConsumo = document.getString("fechaConsumo") ?: ""
                    val lote = document.getString("lote") ?: ""
                    val estado = document.getString("estado") ?: ""
                    val proveedor = document.getString("proveedor") ?: ""
                    val tipoAlimento = document.getString("tipoAlimento") ?: ""
                    val ambiente = document.getString("ambiente") ?: ""

                    val alimento = AlimentoEntity(
                        nombre = nombre,
                        cantidad = cantidad,
                        fechaCaducidad = fechaCaducidad,
                        fechaConsumo = fechaConsumo,
                        lote = lote,
                        estado = estado,
                        proveedor = proveedor,
                        tipoAlimento = tipoAlimento,
                        ambiente = ambiente,
                        ID_usuario = usuarioId
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val yaExiste = alimentoDao.existeAlimentoIgual(alimento.nombre, usuarioId)
                        if (!yaExiste) {
                            alimentoDao.insertarAlimento(alimento)
                        }
                    }
                }
            }
    }

    fun eliminarAlimentoDeFirestore(alimentoId: Int) {
        FirebaseFirestore.getInstance()
            .collection("alimentos")
            .document(alimentoId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreDelete", "Alimento $alimentoId eliminado de Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDelete", "Error al eliminar alimento $alimentoId: ${e.message}")
            }
    }


    fun syncUsuarioToFirestore(usuario: UsuarioEntity) {
        val usuarioMap = mapOf(
            "nombre" to usuario.nombre,
            "correo" to usuario.correo
        )

        firestore.collection("usuarios")
            .document(usuario.id.toString())
            .set(usuarioMap)
            .addOnSuccessListener { println("Usuario subido a Firestore") }
            .addOnFailureListener { e -> println("Error al subir usuario: ${e.message}") }
    }


    suspend fun eliminarUsuarioDeFirestore(userId: Int) {
        val db = Firebase.firestore


        db.collection("alimentos")
            .whereEqualTo("ID_usuario", userId)
            .get()
            .await()
            .documents.forEach { it.reference.delete().await() }


        db.collection("alertas")
            .whereEqualTo("ID_usuario", userId)
            .get()
            .await()
            .documents.forEach { it.reference.delete().await() }


        db.collection("usuarios").document(userId.toString()).delete().await()
    }



    fun syncAlertaToFirestore(alerta: AlertaEntity) {
        val alertaMap = mapOf(
            "ID_usuario" to alerta.ID_usuario,
            "mensaje" to alerta.mensaje,
            "fecha" to alerta.fecha,
            "tipo" to alerta.tipo
        )

        firestore.collection("alertas")
            .add(alertaMap)
            .addOnSuccessListener { println(" Alerta subida a Firestore") }
            .addOnFailureListener { e -> println("Error al subir alerta: ${e.message}") }
    }
}

