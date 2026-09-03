package com.creditphone.app

import android.content.Context

object Prefs {
    private const val NOMBRE = "creditphone_prefs"
    private const val CLAVE_BASE_URL = "base_url"
    private const val CLAVE_DEVICE_TOKEN = "device_token"

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        return prefs.getString(CLAVE_BASE_URL, "") ?: ""
    }

    fun setBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE).edit()
            .putString(CLAVE_BASE_URL, url).apply()
    }

    fun getDeviceToken(context: Context): String {
        val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
        return prefs.getString(CLAVE_DEVICE_TOKEN, "") ?: ""
    }

    fun setDeviceToken(context: Context, token: String) {
        context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE).edit()
            .putString(CLAVE_DEVICE_TOKEN, token).apply()
    }
}
