package com.pamelapp.memorias.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
  
  companion object {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
  }
  
  // Lectura reactiva: emite cada vez que cambia
  val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
  val userName: Flow<String?> = context.dataStore.data.map { it[NAME_KEY] }
  val userId: Flow<Int?> = context.dataStore.data.map { it[USER_ID_KEY] }
  val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }
  
  suspend fun save(token: String, name: String, userId: Int, userEmail:String) {
    context.dataStore.edit { prefs ->
      prefs[TOKEN_KEY] = token
      prefs[NAME_KEY] = name
      prefs[USER_ID_KEY] = userId
      prefs[USER_EMAIL_KEY] = userEmail
    }
  }
  
  suspend fun clear() {
    context.dataStore.edit { it.clear() }
  }
}