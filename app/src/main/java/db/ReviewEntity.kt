package db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review")
data class ReviewEntity(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "comment") val comment: String?,
    @ColumnInfo(name = "rating") val rating: Int,
    @ColumnInfo(name = "showId") val showId: Int,
    @ColumnInfo(name = "userId") val userId: String,
    @ColumnInfo(name = "userEmail") val userEmail: String,
    @ColumnInfo(name = "userImageUrl") val userImageUrl: String?
)

