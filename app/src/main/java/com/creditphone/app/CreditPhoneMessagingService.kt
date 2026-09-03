package com.creditphone.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CreditPhoneMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val baseUrl = Prefs.getBaseUrl(applicationContext)
        val deviceToken = Prefs.getDeviceToken(applicationContext)
        if (baseUrl.isNotBlank() && deviceToken.isNotBlank()) {
            ApiClient.actualizarToken(baseUrl, deviceToken, token, object : ApiClient.Callback2 {
                override fun onSuccess(body: org.json.JSONObject) {}
                override fun onError(mensaje: String) {}
            })
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val tipo = message.data["tipo"] ?: return
        val motivo = message.data["motivo"] ?: ""

        when (tipo) {
            "bloquear" -> {
                aplicarBloqueo()
                notificar("Equipo bloqueado", motivo.ifBlank { "El equipo fue bloqueado por mora en los pagos." })
            }
            "desbloquear" -> {
                notificar("Equipo desbloqueado", motivo.ifBlank { "El pago fue confirmado, el equipo está desbloqueado." })
            }
        }
    }

    private fun aplicarBloqueo() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, CreditPhoneDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        }
    }

    private fun notificar(titulo: String, texto: String) {
        val canalId = "creditphone_bloqueo"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "Bloqueo de equipo", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(canal)
        }

        val notificacion = NotificationCompat.Builder(this, canalId)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(1, notificacion)
    }
}
