package infinumacademy.showsapp.kristinakoneva.showsscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import db.ShowsAppDatabase
import java.lang.IllegalArgumentException

class ShowsViewModelFactory(val database: ShowsAppDatabase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ShowsViewModel::class.java)) {
            return ShowsViewModel(database) as T
        }
        throw IllegalArgumentException("Cannot work with non ShowsViewModel classes!")
    }
}
