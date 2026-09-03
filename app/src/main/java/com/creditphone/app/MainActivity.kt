package com.creditphone.app

import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var deviceUid: String
    private lateinit var admin: ComponentName
    private lateinit var dpm: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        deviceUid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        admin = ComponentName(this, CreditPhoneDeviceAdminReceiver::class.java)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val txtDeviceUid = findViewById<TextView>(R.id.txtDeviceUid)
        val btnCopiarUid = findViewById<Button>(R.id.btnCopiarUid)
        val editBaseUrl = findViewById<EditText>(R.id.editBaseUrl)
        val btnGuardarUrl = findViewById<Button>(R.id.btnGuardarUrl)
        val txtEstadoAdmin = findViewById<TextView>(R.id.txtEstadoAdmin)
        val btnActivarAdmin = findViewById<Button>(R.id.btnActivarAdmin)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val txtResultado = findViewById<TextView>(R.id.txtResultado)

        txtDeviceUid.text = deviceUid
        editBaseUrl.setText(Prefs.getBaseUrl(this))

        fun actualizarEstadoAdmin() {
            txtEstadoAdmin.text = if (dpm.isAdminActive(admin)) {
                "Administrador de dispositivo: ACTIVO"
            } else {
                "Administrador de dispositivo: no activo"
            }
        }
        actualizarEstadoAdmin()

        btnCopiarUid.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("deviceUid", deviceUid))
            Toast.makeText(this, "ID copiado", Toast.LENGTH_SHORT).show()
        }

        btnGuardarUrl.setOnClickListener {
            Prefs.setBaseUrl(this, editBaseUrl.text.toString().trim())
            Toast.makeText(this, "Dirección guardada", Toast.LENGTH_SHORT).show()
        }

        btnActivarAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "CreditPhone necesita este permiso para poder bloquear el equipo remotamente en caso de mora en los pagos."
            )
            startActivity(intent)
        }

        btnRegistrar.setOnClickListener {
            val baseUrl = Prefs.getBaseUrl(this)
            if (baseUrl.isBlank()) {
                txtResultado.text = "Primero guardá la dirección del backend."
                return@setOnClickListener
            }
            if (!dpm.isAdminActive(admin)) {
                txtResultado.text = "Primero activá el administrador de dispositivo (paso 1)."
                return@setOnClickListener
            }

            txtResultado.text = "Obteniendo token de notificaciones..."
            FirebaseMessaging.getInstance().token.addOnCompleteListener { tarea ->
                if (!tarea.isSuccessful) {
                    txtResultado.text = "No se pudo obtener el token de Firebase: ${tarea.exception?.message}"
                    return@addOnCompleteListener
                }
                val fcmToken = tarea.result

                ApiClient.activarDispositivo(baseUrl, deviceUid, fcmToken, object : ApiClient.Callback2 {
                    override fun onSuccess(body: JSONObject) {
                        runOnUiThread {
                            val token = body.optString("token")
                            Prefs.setDeviceToken(this@MainActivity, token)
                            val estado = body.optString("estado")
                            txtResultado.text = "Equipo registrado correctamente. Estado actual: $estado"
                        }
                    }

                    override fun onError(mensaje: String) {
                        runOnUiThread {
                            txtResultado.text = "Error al registrar: $mensaje"
                        }
                    }
                })
            }
        }

        actualizarEstadoAdmin()
    }

    override fun onResume() {
        super.onResume()
        val txtEstadoAdmin = findViewById<TextView>(R.id.txtEstadoAdmin)
        txtEstadoAdmin.text = if (dpm.isAdminActive(admin)) {
            "Administrador de dispositivo: ACTIVO"
        } else {
            "Administrador de dispositivo: no activo"
        }
    }
}
