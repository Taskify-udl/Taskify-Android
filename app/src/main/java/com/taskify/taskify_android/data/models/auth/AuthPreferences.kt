package com.taskify.taskify_android.data.models.auth


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object AuthPreferences {
    private val TOKEN_KEY = stringPreferencesKey("api_token")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    fun getTokenBlocking(context: Context): String? = runBlocking {
        val prefs = context.dataStore.data.first()
        prefs[TOKEN_KEY]
    }
}
