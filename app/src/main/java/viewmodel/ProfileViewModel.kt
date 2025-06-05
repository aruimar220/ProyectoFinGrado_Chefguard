import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tuapp.data.remote.FirestoreSyncHelper
import data.local.AppDatabase
import kotlinx.coroutines.launch

class ProfileViewModel(private val db: AppDatabase) : ViewModel() {
    fun eliminarCuenta(userId: Int, onAccountDeleted: () -> Unit) { // Función para eliminar la cuenta del usuario y sincronizar con Firestore
        viewModelScope.launch {
            FirestoreSyncHelper.eliminarUsuarioDeFirestore(userId) // Elimina el usuario de Firestore utilizando FirestoreSyncHelper y el ID del usuario proporcionado como parámetro
            db.usuarioDao().eliminarUsuarioPorId(userId) // Elimina el usuario de la base de datos local utilizando el DAO de usuario y el ID del usuario proporcionado como parámetro
            onAccountDeleted() // Llama a la función de retorno proporcionada para indicar que la cuenta ha sido eliminada
        }
    }


    companion object {
        fun provideFactory(db: AppDatabase): ViewModelProvider.Factory { // Fábrica de proveedores de vista del modelo para crear instancias de ProfileViewModel
            return object : ViewModelProvider.Factory { // Implementación de ViewModelProvider.Factory para crear instancias de ProfileViewModel con la base de datos proporcionada
                override fun <T : ViewModel> create(modelClass: Class<T>): T { // Sobrescribe el metodo create para crear instancias de ProfileViewModel con la base de datos proporcionada
                    if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) { // Verifica si el modelo proporcionado es de la clase ProfileViewModel
                        @Suppress("UNCHECKED_CAST") // Supresión de cast no necesario debido a la verificación anterior de tipo
                        return ProfileViewModel(db) as T // Crea una instancia de ProfileViewModel con la base de datos proporcionada y la devuelve como T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class") // Lanza una excepción si el modelo proporcionado no es de la clase ProfileViewModel y muestra un mensaje de error
                }
            }
        }
    }
}