package com.creditphone.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LockScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val motivo = Prefs.getMotivoBloqueo(this).ifBlank {
            "Este equipo ha sido bloqueado por mora en los pagos. Contacta a tu vendedor para más información."
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            setPadding(64, 64, 64, 64)
        }

        val titulo = TextView(this).apply {
            text = "Equipo Bloqueado"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val mensaje = TextView(this).apply {
            text = motivo
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        layout.addView(titulo)
        layout.addView(mensaje)
        setContentView(layout)
    }

    override fun onBackPressed() {
        // Bloqueado: el botón "atrás" no hace nada
    }

    override fun onPause() {
        super.onPause()
        if (Prefs.getBloqueado(this)) {
            val intent = Intent(this, LockScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
}
