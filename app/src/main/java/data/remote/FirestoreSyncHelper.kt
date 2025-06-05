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
    // Instancia global de Firestore para sincronizar datos con Firestore
    private val firestore = FirebaseFirestore.getInstance()

    fun syncAlimento(alimento: AlimentoEntity) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return // Obtener el correo electrónico del usuario actual
        firestore.collection("alimentos") // Nombre de la colección en Firestore para alimentos
            .document(alimento.id.toString())
            .set(
                mapOf( // Datos del alimento a subir a Firestore en formato clave-valor
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
            .addOnSuccessListener { Log.d("Firestore", "Alimento subido para $userEmail") } // Mensaje de éxito
            .addOnFailureListener { e -> Log.e("Firestore", "Error al subir alimento", e) } // Manejo de errores
    }
//Descarga todos los documentos de la colección “alimentos” en Firestore que tengan el campo “correo” igual al email del usuario autenticado.
    fun sincronizarAlimentosDesdeFirestore(context: Context) {
        val userEmail   = FirebaseAuth.getInstance().currentUser?.email ?: return // Obtener el correo electrónico del usuario actual
        val dbLocal     = AppDatabase.getDatabase(context) // Obtener la instancia de la base de datos local
        val dao         = dbLocal.alimentoDao() // Obtener el DAO de alimentos de la base de datos local
        val localUserId = com.example.chefguard.utils.PreferencesManager.getUserId(context) // Obtener el ID del usuario local desde las preferencias compartidas

        firestore.collection("alimentos") // Nombre de la colección en Firestore para alimentos
            .whereEqualTo("correo", userEmail) // Filtrar por el correo electrónico del usuario actual en el campo "correo"
            .get()
            .addOnSuccessListener { result -> // Manejar la respuesta exitosa de la consulta
                for (doc in result) { // Recorrer cada documento en la respuesta
                    val nombre         = doc.getString("nombre") ?: continue
                    val cantidad       = doc.getDouble("cantidad")?.toInt() ?: continue
                    val fechaCaducidad = doc.getString("fechaCaducidad") ?: continue
                    val fechaConsumo   = doc.getString("fechaConsumo") ?: ""
                    val lote           = doc.getString("lote") ?: ""
                    val estado         = doc.getString("estado") ?: ""
                    val proveedor      = doc.getString("proveedor") ?: ""
                    val tipoAlimento   = doc.getString("tipoAlimento") ?: ""
                    val ambiente       = doc.getString("ambiente") ?: ""

                    val alimento = AlimentoEntity( // Crear un objeto AlimentoEntity con los datos del documento
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

                    CoroutineScope(Dispatchers.IO).launch { // Ejecutar en un hilo de fondo para evitar bloquear el hilo principal de la interfaz de usuario
                        val existe = dao.existeAlimentoIgual(nombre, localUserId) // Verificar si el alimento ya existe en la base de datos local
                        if (!existe) { // Si no existe, insertar el alimento en la base de datos local
                            dao.insertarAlimento(alimento) // Insertar el alimento en la base de datos local con el ID del usuario local
                        }
                    }
                }
            }
            .addOnFailureListener { e -> Log.e("Firestore", "Error al descargar alimentos", e) } // Manejo de errores
    }
    // Función para eliminar alimentos de firestore
    fun eliminarAlimentoDeFirestore(alimentoId: Int) {
        FirebaseFirestore.getInstance()
            .collection("alimentos")
            .document(alimentoId.toString())
            .delete() // Eliminar el documento correspondiente al alimentoId
            .addOnSuccessListener {
                Log.d("FirestoreDelete", "Alimento $alimentoId eliminado de Firestore") // Mensaje de éxito
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDelete", "Error al eliminar alimento $alimentoId: ${e.message}") // Manejo de errores
            }
    }

    // Función para sincronizar usuarios a Firestore
    fun syncUsuarioToFirestore(usuario: UsuarioEntity) {
        val usuarioMap = mapOf( // Datos del usuario a subir a Firestore en formato clave-valor
            "nombre" to usuario.nombre,
            "correo" to usuario.correo
        )

        firestore.collection("usuarios") // Nombre de la colección en Firestore para usuarios
            .document(usuario.id.toString()) // Utilizar el ID del usuario como identificador único en Firestore
            .set(usuarioMap) // Subir los datos del usuario a Firestore
            .addOnSuccessListener { println("Usuario subido a Firestore") } // Mensaje de éxito
            .addOnFailureListener { e -> println("Error al subir usuario: ${e.message}") } // Manejo de errores
    }

    // Función para eliminar usuarios de firestore
    suspend fun eliminarUsuarioDeFirestore(userId: Int) {
        val db = Firebase.firestore // Obtener una instancia de Firestore desde Firebase

        // Eliminar todos los alimentos asociados al usuario en Firestore
        db.collection("alimentos")
            .whereEqualTo("ID_usuario", userId)
            .get()
            .await()
            .documents.forEach { it.reference.delete().await() }

        // Eliminar todos las alertas asociadas al usuario en Firestore
        db.collection("alertas")
            .whereEqualTo("ID_usuario", userId)
            .get()
            .await()
            .documents.forEach { it.reference.delete().await() }

        // Eliminar el usuario en Firestore
        db.collection("usuarios").document(userId.toString()).delete().await()
    }

}

