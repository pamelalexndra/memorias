package com.example.pamelapp.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.example.pamelapp.domain.model.DeleteMode

class SwipePreferences(
    context: Context
) {
    private val prefsKey = "memorias_prefs"
    private val deleteModeKey = "delete_mode"
    private val showSwipeButtonsKey = "show_swipe_buttons"

    private val authTokenKey = "auth_token"
    private val refreshTokenKey = "refresh_token"
    private val userEmailKey = "user_email"
    private val userDisplayNameKey = "user_display_name"

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

    fun saveAuthSession(
        accessToken: String,
        refreshToken: String?,
        email: String,
        displayName: String?
    ) {
        prefs.edit {
            putString(authTokenKey, accessToken)
            putString(refreshTokenKey, refreshToken)
            putString(userEmailKey, email)
            putString(userDisplayNameKey, displayName)
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(authTokenKey, null)
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    fun clearAuthSession() {
        prefs.edit {
            remove(authTokenKey)
            remove(refreshTokenKey)
            remove(userEmailKey)
            remove(userDisplayNameKey)
        }
    }
}