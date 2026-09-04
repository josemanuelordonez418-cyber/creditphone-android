package com.creditphone.app

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object ApiClient {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    interface Callback2 {
        fun onSuccess(body: JSONObject)
        fun onError(mensaje: String)
    }

    fun activarDispositivo(baseUrl: String, deviceUid: String, fcmToken: String, cb: Callback2) {
        val body = JSONObject()
        body.put("deviceUid", deviceUid)
        body.put("fcmToken", fcmToken)

        val request = Request.Builder()
            .url("$baseUrl/api/devices/activar")
            .post(body.toString().toRequestBody(JSON))
            .build()

        ejecutar(request, cb)
    }

    fun consultarEstado(baseUrl: String, deviceToken: String, cb: Callback2) {
        val request = Request.Builder()
            .url("$baseUrl/api/devices/estado")
            .header("Authorization", "Bearer $deviceToken")
            .get()
            .build()

        ejecutar(request, cb)
    }

    fun actualizarToken(baseUrl: String, deviceToken: String, fcmToken: String, cb: Callback2) {
        val body = JSONObject()
        body.put("fcmToken", fcmToken)

        val request = Request.Builder()
            .url("$baseUrl/api/devices/token")
            .header("Authorization", "Bearer $deviceToken")
            .put(body.toString().toRequestBody(JSON))
            .build()

        ejecutar(request, cb)
    }

    private fun ejecutar(request: Request, cb: Callback2) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cb.onError(e.message ?: "Error de red")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val texto = response.body?.string() ?: "{}"
                if (!response.isSuccessful) {
                    cb.onError("Error ${response.code}: $texto")
                    return
                }
                try {
                    cb.onSuccess(JSONObject(texto))
                } catch (e: Exception) {
                    cb.onError("Respuesta inesperada: $texto")
                }
            }
        })
    }
}
