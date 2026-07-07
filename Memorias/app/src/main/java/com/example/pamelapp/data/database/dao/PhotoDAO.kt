package com.example.pamelapp.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pamelapp.data.database.entities.FavoritePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDAO {
  @Query("SELECT * FROM favorite_photo")
  fun getFavoritePhotos(): Flow<List<FavoritePhotoEntity>>
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPhoto(favoritePhoto: FavoritePhotoEntity)
  
  @Delete
  suspend fun deletePhoto(favoritePhoto: FavoritePhotoEntity)
  
  @Query("DELETE FROM favorite_photo")
  suspend fun clearFavoritePhotos()
}