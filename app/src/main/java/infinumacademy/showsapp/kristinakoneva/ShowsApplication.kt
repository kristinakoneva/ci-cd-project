package infinumacademy.showsapp.kristinakoneva

import android.app.Application
import db.ShowsAppDatabase
import infinumacademy.showsapp.kristinakoneva.networking.ApiModule

class ShowsApplication : Application() {

    val database by lazy {
        ShowsAppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        ApiModule.initRetrofit(this)
        NetworkLiveData.init(this)
    }
}
