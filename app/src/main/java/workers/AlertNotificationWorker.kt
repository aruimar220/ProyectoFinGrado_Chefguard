import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.chefguard.MainActivity
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

        val message = alimentosPorCaducar.joinToString("\n") { alimento ->
            val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
            val diasRestantes = fechaCaducidad.toEpochDay() - LocalDate.now().toEpochDay()
            "${alimento.nombre} (caduca en $diasRestantes días)"
        }

        val alimento = alimentosPorCaducar.firstOrNull() ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "item_details/${alimento.id}")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Alimentos próximos a caducar")
            .setContentText("Los siguientes alimentos están por caducar pronto:")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
