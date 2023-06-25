package db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import infinumacademy.showsapp.kristinakoneva.Constants

@Database(
    entities = [
        ShowEntity::class,
        ReviewEntity::class
    ],
    version = 3
)
abstract class ShowsAppDatabase : RoomDatabase() {
    companion object {

        @Volatile
        private var INSTANCE: ShowsAppDatabase? = null

        fun getDatabase(context: Context): ShowsAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val database = Room.databaseBuilder(
                    context,
                    ShowsAppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = database
                database
            }
        }
    }

    abstract fun showDao(): ShowDao
    abstract fun reviewDao(): ReviewDao
}
