package com.example.chefguard.utils

import android.content.Context
import data.local.entity.AlimentoEntity
import java.io.File
import java.io.FileWriter
// Función para exportar los alimentos a un archivo CSV
fun exportAlimentosToCsv(context: Context, alimentos: List<AlimentoEntity>): File? {
    return try {
        val csvFile = File(context.cacheDir, "alimentos_export.csv") // Nombre del archivo CSV
        val writer = FileWriter(csvFile) // Escritor de archivos CSV
        // Encabezado del CSV con los nombres de las columnas en el orden correspondiente
        writer.append("ID,ID_usuario,Nombre,Cantidad,FechaCaducidad,FechaConsumo,Lote,Estado,Proveedor,TipoAlimento,Ambiente\n")
        // Recorrer la lista de alimentos y escribe el archivo CSV
        for (alimento in alimentos) {
            writer.append("${alimento.id},")
            writer.append("${alimento.ID_usuario},")
            writer.append("\"${alimento.nombre}\",")
            writer.append("${alimento.cantidad},")
            writer.append("\"${alimento.fechaCaducidad ?: ""}\",")
            writer.append("\"${alimento.fechaConsumo ?: ""}\",")
            writer.append("\"${alimento.lote ?: ""}\",")
            writer.append("\"${alimento.estado ?: ""}\",")
            writer.append("\"${alimento.proveedor ?: ""}\",")
            writer.append("\"${alimento.tipoAlimento ?: ""}\",")
            writer.append("\"${alimento.ambiente ?: ""}\"\n")
        }
        // Cerrar el escritor de archivos
        writer.flush()
        writer.close()

        csvFile
    } catch (e: Exception) { // Manejar cualquier excepción que pueda ocurrir durante la exportación
        e.printStackTrace()
        null
    }
}
