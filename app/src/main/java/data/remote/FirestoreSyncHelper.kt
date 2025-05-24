package com.tuapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import data.local.entity.AlertaEntity
import data.local.entity.AlimentoEntity
import data.local.entity.UsuarioEntity

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

    fun eliminarAlimentoDeFirestore(alimentoId: Int) {
        FirebaseFirestore.getInstance()
            .collection("alimentos")
            .document(alimentoId.toString())
            .delete()
            .addOnSuccessListener { println("Alimento eliminado de Firestore") }
            .addOnFailureListener { e -> println("Error al eliminar: ${e.message}") }
    }

    fun syncUsuarioToFirestore(usuario: UsuarioEntity) {
        val usuarioMap = mapOf(
            "nombre" to usuario.nombre,
            "correo" to usuario.correo
        )

        firestore.collection("usuarios")
            .document(usuario.correo)
            .set(usuarioMap)
            .addOnSuccessListener { println("Usuario subido a Firestore") }
            .addOnFailureListener { e -> println("Error al subir usuario: ${e.message}") }
    }

    fun eliminarUsuarioDeFirestore(userId: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(userId.toString())
            .delete()
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

