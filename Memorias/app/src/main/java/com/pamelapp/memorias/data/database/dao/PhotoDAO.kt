package com.pamelapp.memorias.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pamelapp.memorias.data.database.entities.FavoritePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDAO {
  @Query("SELECT * FROM favorite_photo")
  fun getFavoritePhotos(): Flow<List<FavoritePhotoEntity>>
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPhoto(favoritePhoto: FavoritePhotoEntity)

  @Query("SELECT EXISTS(SELECT 1 FROM favorite_photo WHERE id = :photoId)")
  suspend fun isFavoritePhoto(photoId: Long): Boolean

  @Delete
  suspend fun deletePhoto(favoritePhoto: FavoritePhotoEntity)
  
  @Query("DELETE FROM favorite_photo")
  suspend fun clearFavoritePhotos()

  @Query("DELETE FROM favorite_photo WHERE id IN (:photoIds)")
  suspend fun deletePhotosByIds(photoIds: List<Long>)
  @Query("SELECT * FROM favorite_photo")
  suspend fun getFavoritePhotosOnce(): List<FavoritePhotoEntity>

}