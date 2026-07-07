package com.example.pamelapp.data.database

import android.content.Context
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.pamelapp.data.database.entities.FavoritePhotoEntity
import com.example.pamelapp.data.database.dao.PhotoDAO

@Database(
  entities = [
    FavoritePhotoEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  
  abstract fun favoritePhotoDAO(): PhotoDAO
  
  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null
    
    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        Room.databaseBuilder(
          context = context.applicationContext,
          klass = AppDatabase::class.java,
          name = "FavoritePhotos_database"
        )
          .fallbackToDestructiveMigration(false)
          .build()
          .also { INSTANCE = it }
      }
    }
  }
}

class Converters {
  
  @TypeConverter
  fun fromUri(uri: Uri?): String? {
    return uri?.toString()
  }
  
  @TypeConverter
  fun toUri(uriString: String?): Uri? {
    return uriString?.let { Uri.parse(it) }
  }
  
}