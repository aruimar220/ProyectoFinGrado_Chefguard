import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tuapp.data.remote.FirestoreSyncHelper
import data.local.AppDatabase
import kotlinx.coroutines.launch

class ProfileViewModel(private val db: AppDatabase) : ViewModel() {
    fun eliminarCuenta(userId: Int, onAccountDeleted: () -> Unit) {
        viewModelScope.launch {
            FirestoreSyncHelper.eliminarUsuarioDeFirestore(userId)
            db.usuarioDao().eliminarUsuarioPorId(userId)
            onAccountDeleted()
        }
    }

    companion object {
        fun provideFactory(db: AppDatabase): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ProfileViewModel(db) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}