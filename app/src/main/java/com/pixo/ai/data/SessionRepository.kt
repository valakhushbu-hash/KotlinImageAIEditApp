package com.pixo.ai.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "pixo_session")

data class Session(
    val loggedIn: Boolean = false,
    val guest: Boolean = false,
    val email: String = ""
)

class SessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val loggedIn = booleanPreferencesKey("logged_in")
        val guest = booleanPreferencesKey("guest")
        val email = stringPreferencesKey("email")
    }

    val session: Flow<Session> = context.sessionDataStore.data.map {
        Session(
            loggedIn = it[Keys.loggedIn] ?: false,
            guest = it[Keys.guest] ?: false,
            email = it[Keys.email] ?: ""
        )
    }

    suspend fun login(email: String) {
        context.sessionDataStore.edit {
            it[Keys.loggedIn] = true
            it[Keys.guest] = false
            it[Keys.email] = email
        }
    }

    suspend fun guest() {
        context.sessionDataStore.edit {
            it[Keys.loggedIn] = false
            it[Keys.guest] = true
            it[Keys.email] = ""
        }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { it.clear() }
    }
}
