package com.yzk.githubclient.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * @description security shared preference
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
object SecuritySharedPreference {

    private lateinit var encryptedSharedPreferences: EncryptedSharedPreferences

    fun initSecuritySharedPreference(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveData(key: String, value: String) {
        encryptedSharedPreferences.edit().putString(key, value).apply()
    }

    fun getData(key: String): String? {
        return encryptedSharedPreferences.getString(key, "")
    }

    fun clearData(key: String) {
        encryptedSharedPreferences.edit().remove(key).apply()
    }
}