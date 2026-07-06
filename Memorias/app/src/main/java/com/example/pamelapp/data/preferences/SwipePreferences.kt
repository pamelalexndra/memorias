package com.example.pamelapp.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.example.pamelapp.domain.model.DeleteMode

class SwipePreferences(
    context: Context
) {
    private val prefsKey = "memorias_prefs"
    private val favoritesKey = "favorites"
    private val deleteModeKey = "delete_mode"
    private val showSwipeButtonsKey = "show_swipe_buttons"

    private val prefs = context.getSharedPreferences(
        prefsKey,
        Context.MODE_PRIVATE
    )

    fun getDeleteMode(): DeleteMode {
        return when (prefs.getString(deleteModeKey, DeleteMode.TRASH.name)) {
            DeleteMode.PERMANENT.name -> DeleteMode.PERMANENT
            else -> DeleteMode.TRASH
        }
    }

    fun setDeleteMode(mode: DeleteMode) {
        prefs.edit {
            putString(deleteModeKey, mode.name)
        }
    }

    fun getShowSwipeButtons(): Boolean {
        return prefs.getBoolean(showSwipeButtonsKey, true)
    }

    fun setShowSwipeButtons(show: Boolean) {
        prefs.edit {
            putBoolean(showSwipeButtonsKey, show)
        }
    }

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(favoritesKey, emptySet()).orEmpty()
    }

    fun setFavoriteIds(favoriteIds: Set<String>) {
        prefs.edit {
            putStringSet(favoritesKey, favoriteIds)
        }
    }

    fun toggleFavorite(photoId: Long): Set<String> {
        val favoriteIds = getFavoriteIds().toMutableSet()
        val photoIdText = photoId.toString()

        if (favoriteIds.contains(photoIdText)) {
            favoriteIds.remove(photoIdText)
        } else {
            favoriteIds.add(photoIdText)
        }

        setFavoriteIds(favoriteIds)

        return favoriteIds
    }

    fun restoreFavoriteState(
        photoId: Long,
        shouldBeFavorite: Boolean
    ): Set<String> {
        val favoriteIds = getFavoriteIds().toMutableSet()
        val photoIdText = photoId.toString()

        if (shouldBeFavorite) {
            favoriteIds.add(photoIdText)
        } else {
            favoriteIds.remove(photoIdText)
        }

        setFavoriteIds(favoriteIds)

        return favoriteIds
    }

    fun removeFavorites(photoIds: Set<Long>): Set<String> {
        val idsToRemove = photoIds.map { it.toString() }.toSet()
        val favoriteIds = getFavoriteIds().toMutableSet()

        favoriteIds.removeAll(idsToRemove)

        setFavoriteIds(favoriteIds)

        return favoriteIds
    }

    fun clearFavorites() {
        prefs.edit {
            remove(favoritesKey)
        }
    }
}