package com.example.chefguard.utils

import android.content.Context
import com.example.chefguard.model.AlimentoEntity
import java.io.File
import java.io.FileWriter

fun exportAlimentosToCsv(context: Context, alimentos: List<AlimentoEntity>): File? {
    return try {
        val csvFile = File(context.cacheDir, "alimentos_export.csv")
        val writer = FileWriter(csvFile)

        writer.append("ID,ID_usuario,Nombre,Cantidad,FechaCaducidad,FechaConsumo,Lote,Estado,Proveedor,TipoAlimento,Ambiente\n")

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

        writer.flush()
        writer.close()

        csvFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
