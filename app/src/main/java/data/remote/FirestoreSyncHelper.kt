package com.tuapp.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        firestore.collection("alimentos")
            .document(alimento.id.toString())
            .set(
                mapOf(
                    "nombre"        to alimento.nombre,
                    "cantidad"      to alimento.cantidad,
                    "fechaCaducidad" to alimento.fechaCaducidad,
                    "fechaConsumo"  to alimento.fechaConsumo,
                    "lote"          to alimento.lote,
                    "estado"        to alimento.estado,
                    "proveedor"     to alimento.proveedor,
                    "tipoAlimento"  to alimento.tipoAlimento,
                    "ambiente"      to alimento.ambiente,
                    "correo"        to userEmail
                )
            )
            .addOnSuccessListener { Log.d("Firestore", "Alimento subido para $userEmail") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error al subir alimento", e) }
    }

    fun sincronizarAlimentosDesdeFirestore(context: Context) {
        val userEmail   = FirebaseAuth.getInstance().currentUser?.email ?: return
        val dbLocal     = AppDatabase.getDatabase(context)
        val dao         = dbLocal.alimentoDao()
        val localUserId = com.example.chefguard.utils.PreferencesManager.getUserId(context)

        firestore.collection("alimentos")
            .whereEqualTo("correo", userEmail)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val nombre         = doc.getString("nombre") ?: continue
                    val cantidad       = doc.getDouble("cantidad")?.toInt() ?: continue
                    val fechaCaducidad = doc.getString("fechaCaducidad") ?: continue
                    val fechaConsumo   = doc.getString("fechaConsumo") ?: ""
                    val lote           = doc.getString("lote") ?: ""
                    val estado         = doc.getString("estado") ?: ""
                    val proveedor      = doc.getString("proveedor") ?: ""
                    val tipoAlimento   = doc.getString("tipoAlimento") ?: ""
                    val ambiente       = doc.getString("ambiente") ?: ""

                    val alimento = AlimentoEntity(
                        nombre        = nombre,
                        cantidad      = cantidad,
                        fechaCaducidad= fechaCaducidad,
                        fechaConsumo  = fechaConsumo,
                        lote          = lote,
                        estado        = estado,
                        proveedor     = proveedor,
                        tipoAlimento  = tipoAlimento,
                        ambiente      = ambiente,
                        ID_usuario    = localUserId
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val existe = dao.existeAlimentoIgual(nombre, localUserId)
                        if (!existe) {
                            dao.insertarAlimento(alimento)
                        }
                    }
                }
            }
            .addOnFailureListener { e -> Log.e("Firestore", "Error al descargar alimentos", e) }
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

