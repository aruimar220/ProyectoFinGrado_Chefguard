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

class AlertNotificationWorker( // Clase para manejar las notificaciones de alerta de caducidad
    private val context: Context, // Contexto de la aplicación para acceder a recursos del sistema y cargar datos
    params: WorkerParameters // Parámetros del trabajador para personalizar su comportamiento específico
) : Worker(context, params) {

    override fun doWork(): Result { // Metodo que se ejecuta en un hilo de fondo para realizar el trabajo en segundo plano
        return try {
            runBlocking { // Ejecuta el código dentro del bloque en un hilo de fondo para evitar bloquear el hilo principal
                val db = AppDatabase.getDatabase(context) // Instancia de la base de datos local de la aplicación
                val userId = PreferencesManager.getUserId(context) // ID del usuario actual en la aplicación para obtener sus alimentos

                if (userId == -1) { // Si el usuario no está autenticado, devuelve un resultado de fallo para evitar mostrar la notificación
                    return@runBlocking Result.failure() // Devuelve un resultado de fallo para evitar mostrar la notificación
                }

                val alimentos = withContext(Dispatchers.IO) { // Obtiene los alimentos del usuario actual en un hilo de fondo
                    db.alimentoDao().obtenerAlimentosPorUsuario(userId) // Obtiene los alimentos del usuario actual desde la base de datos
                }

                val hoy = LocalDate.now() // Obtiene la fecha actual para filtrar los alimentos por caducidad en el próximo día
                val alimentosPorCaducar = alimentos.filter { alimento ->
                    try {
                        val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE) // Convierte la fecha de caducidad del alimento a un objeto LocalDate para compararlo con la fecha actual
                        fechaCaducidad.isEqual(hoy) || // Comprueba si el alimento caduca hoy o dentro de los próximos dos días
                        fechaCaducidad.isAfter(hoy.minusDays(1)) && fechaCaducidad.isBefore(hoy.plusDays(2)) // Comprueba si el alimento caduca dentro de los próximos dos días
                    } catch (e: Exception) {
                        false // Si hay un error al convertir la fecha de caducidad, se considera que el alimento no caduca dentro de los próximos dos días
                    }
                }

                if (alimentosPorCaducar.isNotEmpty()) {
                    showGroupedNotification(alimentosPorCaducar) // Muestra la notificación de alerta si hay alimentos por caducar en el próximo día
                }

                Result.success() // Devuelve un resultado de éxito para indicar que el trabajo se completó correctamente
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showGroupedNotification(alimentos: List<AlimentoEntity>) { // Muestra la notificación de alerta con la lista de alimentos por caducar en el próximo día
        val channelId = "alert_channel" // ID del canal de notificación para la notificación de alerta
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager // Obtiene el servicio de notificaciones del sistema para mostrar la notificación

        val intent = Intent(context, MainActivity::class.java).apply { // Crea un intent para abrir la actividad principal de la aplicación al hacer clic en la notificación
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Define las banderas del intent para limpiar la pila de actividades y lanza la actividad principal

            if (alimentos.size == 1) { // Si hay un solo alimento por caducar, agrega la información del alimento a la notificación y navega a la pantalla de detalles del alimento
                putExtra("navigate_to", "item_details/${alimentos.first().id}") // Agrega el ID del alimento a la notificación para navegar a la pantalla de detalles del alimento
            } else {
                putExtra("navigate_to", "alerts") // Si hay más de un alimento por caducar, navega a la pantalla de alertas
            }
        }

        val pendingIntent = requireNotNull(TaskStackBuilder.create(context).run { // Crea un intent para la actividad principal de la aplicación al hacer clic en la notificación
            addNextIntentWithParentStack(intent) // Agrega la actividad principal a la pila de actividades para manejar la navegación hacia atrás
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) // Obtiene el intent como un PendingIntent para lanzar la actividad principal
        })

        val mensaje = if (alimentos.size == 1) { // Construye el mensaje de notificación según el número de alimentos por caducar en el próximo día
            val alimento = alimentos.first() // Obtiene el primer alimento de la lista de alimentos por caducar
            val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE) // Convierte la fecha de caducidad del alimento a un objeto LocalDate para calcular los días restantes
            val diasRestantes = fechaCaducidad.toEpochDay() - LocalDate.now().toEpochDay() // Calcula los días restantes para caducar el alimento en el próximo día
            "${alimento.nombre} caduca en $diasRestantes días" // Construye el mensaje de notificación con el nombre del alimento y los días restantes
        } else {
            "Tienes ${alimentos.size} alimentos por caducar" //
        }

        val notification = NotificationCompat.Builder(context, channelId) // Construye la notificación con el mensaje y el intent
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Define el icono de la notificación como un icono de información
            .setContentTitle("Alerta de caducidad") // Define el título de la notificación como "Alerta de caducidad"
            .setContentText(mensaje) // Define el texto de la notificación como el mensaje de notificación construído anteriormente
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Define la prioridad de la notificación como alta para su visualización
            .setContentIntent(pendingIntent) // Define el intent para la actividad principal de la aplicación al hacer clic en la notificación
            .setAutoCancel(true) // Desactiva la cancelación automática de la notificación al hacer clic en ella
            .build() // Construye la notificación

        notificationManager.notify(9999, notification) // Muestra la notificación con el ID 9999 en el sistema de notificaciones
    }
}
