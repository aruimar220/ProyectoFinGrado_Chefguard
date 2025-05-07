import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.chefguard.model.AlimentoEntity
import com.example.chefguard.model.AppDatabase
import com.example.chefguard.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AlertNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val result = runBlocking {
                val db = AppDatabase.getDatabase(context)
                val userId = PreferencesManager.getUserId(context)

                if (userId == -1) {
                    return@runBlocking Result.failure()
                }

                val alimentos = withContext(Dispatchers.IO) {
                    db.alimentoDao().obtenerAlimentosPorUsuario(userId)
                }

                val hoy = LocalDate.now()
                val alimentosPorCaducar = alimentos.filter { alimento ->
                    try {
                        val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
                        fechaCaducidad.isAfter(hoy) && fechaCaducidad.isBefore(hoy.plusDays(2))
                    } catch (e: Exception) {
                        false
                    }
                }

                if (alimentosPorCaducar.isNotEmpty()) {
                    showNotification(alimentosPorCaducar)
                }

                Result.success()
            }

            result
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(alimentosPorCaducar: List<AlimentoEntity>) {
        val notificationId = 1
        val channelId = "alert_channel"

        // Crear un mensaje con los nombres de los alimentos y los días que quedan para caducar
        val message = alimentosPorCaducar.joinToString("\n") { alimento ->
            val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
            val diasRestantes = fechaCaducidad.toEpochDay() - LocalDate.now().toEpochDay()
            "${alimento.nombre} (caduca en $diasRestantes días)"
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Alimentos próximos a caducar")
            .setContentText("Los siguientes alimentos están por caducar pronto:")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Usar BigTextStyle para mostrar más texto
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}