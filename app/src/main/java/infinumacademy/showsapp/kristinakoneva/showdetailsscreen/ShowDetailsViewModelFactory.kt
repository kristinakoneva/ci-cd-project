package infinumacademy.showsapp.kristinakoneva.showdetailsscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import db.ShowsAppDatabase
import java.lang.IllegalArgumentException

class ShowDetailsViewModelFactory(
    val database: ShowsAppDatabase,
    val showId: Int
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ShowDetailsViewModel::class.java)) {
            return ShowDetailsViewModel(database, showId) as T
        }
        throw IllegalArgumentException("Cannot work with non ShowDetailsViewModel classes!")
    }
}
