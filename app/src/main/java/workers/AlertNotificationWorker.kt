import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.chefguard.MainActivity
import data.local.entity.AlimentoEntity
import data.local.AppDatabase
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
            runBlocking {
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
                        fechaCaducidad.isEqual(hoy) ||
                        fechaCaducidad.isAfter(hoy.minusDays(1)) && fechaCaducidad.isBefore(hoy.plusDays(2))
                    } catch (e: Exception) {
                        false
                    }
                }

                if (alimentosPorCaducar.isNotEmpty()) {
                    showGroupedNotification(alimentosPorCaducar)
                }

                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showGroupedNotification(alimentos: List<AlimentoEntity>) {
        val channelId = "alert_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            if (alimentos.size == 1) {
                putExtra("navigate_to", "item_details/${alimentos.first().id}")
            } else {
                putExtra("navigate_to", "alerts")
            }
        }

        val pendingIntent = requireNotNull(TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        })

        val mensaje = if (alimentos.size == 1) {
            val alimento = alimentos.first()
            val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
            val diasRestantes = fechaCaducidad.toEpochDay() - LocalDate.now().toEpochDay()
            "${alimento.nombre} caduca en $diasRestantes días"
        } else {
            "Tienes ${alimentos.size} alimentos por caducar"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Alerta de caducidad")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(9999, notification)
    }
}
