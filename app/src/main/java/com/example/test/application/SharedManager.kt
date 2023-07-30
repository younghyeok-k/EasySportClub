package com.example.test.application

import android.content.Context
import android.content.SharedPreferences
import com.example.test.application.PreferenceHelper.get
import com.example.test.application.PreferenceHelper.set
import com.example.test.model.User
import com.google.gson.Gson


object SharedManager {
    private lateinit var prefs: SharedPreferences
    private lateinit var instance: SharedManager

    fun init(context: Context) {
        prefs = PreferenceHelper.defaultPrefs(context)
    }

    fun getInstance(): SharedManager {
        if (!::instance.isInitialized) {
            instance = SharedManager
        }
        return instance
    }

    fun saveCurrentUser(user: User?) {
        if (user != null) {
            prefs["user"] = Gson().toJson(user)
        } else {
            prefs.edit().remove("user").apply()
        }
    }

    fun getCurrentUser(): User? {
        val json = prefs["user", ""]
        if (json.isNotBlank()) {
            return Gson().fromJson(json, User::class.java)
        }

        return null
    }

    fun saveBearerToken(token: String?) {
        if (token != null) {
            prefs["bearerToken"] = token
        } else {
            prefs.edit().remove("bearerToken").apply()
        }
    }

    fun getBearerToken(): String {
        return prefs["bearerToken", ""]
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
