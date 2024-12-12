import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clerami.universe.ui.addnewdicussion.AddNewViewModel

class AddNewViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the modelClass is AddNewViewModel
        if (modelClass.isAssignableFrom(AddNewViewModel::class.java)) {
            return AddNewViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
