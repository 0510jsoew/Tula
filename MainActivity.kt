package com.nicovideostudio

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.max

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var topicInput: EditText
    private lateinit var durationSpinner: Spinner
    private lateinit var scriptView: TextView
    private lateinit var statusView: TextView
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var latestScript = ""
    private val bg = Color.rgb(15, 18, 35)
    private val panel = Color.rgb(28, 33, 57)
    private val accent = Color.rgb(91, 115, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 198, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }
        root.addView(text("🎬 NICO VIDEO STUDIO", 25f, white, true, Gravity.CENTER))
        root.addView(text("Tu fábrica de videos de Nico", 15f, muted, false, Gravity.CENTER).apply {
            setPadding(0, dp(8), 0, dp(20))
        })
        root.addView(TextView(this).apply {
            text = "Crea un guion, escucha una narración y genera una animación básica de Nico."
            textSize = 14f; setTextColor(muted); setPadding(dp(14), dp(14), dp(14), dp(14)); setBackgroundColor(panel)
        })
        root.addView(label("Tema del video"))
        topicInput = EditText(this).apply {
            hint = "Ej.: 5 hábitos que te hacen pobre"; setHintTextColor(Color.GRAY); setTextColor(white)
            textSize = 16f; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setPadding(dp(12), dp(10), dp(12), dp(10)); setBackgroundColor(panel)
        }
        root.addView(topicInput, matchWrap())
        root.addView(label("Duración objetivo"))
        durationSpinner = Spinner(this)
        durationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("1 minuto", "3 minutos", "5 minutos", "10 minutos"))
        root.addView(durationSpinner, matchWrap())
        val generate = button("1. GENERAR GUION")
        root.addView(generate, matchWrap())
        generate.setOnClickListener {
            val topic = topicInput.text.toString().trim()
            if (topic.isBlank()) { topicInput.error = "Escribe el tema del video"; return@setOnClickListener }
            latestScript = makeScript(topic, durationSpinner.selectedItem.toString().substringBefore(" ").toInt())
            scriptView.text = latestScript
            statusView.text = "Guion listo. Puedes escucharlo o crear una animación de muestra."
        }
        scriptView = TextView(this).apply {
            text = "Aquí aparecerá tu guion."; textSize = 14f; setTextColor(white)
            setPadding(dp(14), dp(14), dp(14), dp(14)); setBackgroundColor(panel)
        }
        root.addView(scriptView, LinearLayout.LayoutParams(-1, dp(240)).apply { topMargin = dp(12) })
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val speak = button("2. ESCUCHAR")
        val animate = button("3. ANIMAR")
        row.addView(speak, LinearLayout.LayoutParams(0, dp(52), 1f).apply { rightMargin = dp(5) })
        row.addView(animate, LinearLayout.LayoutParams(0, dp(52), 1f).apply { leftMargin = dp(5) })
        root.addView(row)
        speak.setOnClickListener {
            if (latestScript.isBlank()) statusView.text = "Primero genera un guion."
            else if (ttsReady) {
                tts?.speak(latestScript.take(3500), TextToSpeech.QUEUE_FLUSH, null, "nico-script")
                statusView.text = "Reproduciendo la voz disponible en tu Android."
            } else statusView.text = "La voz del teléfono se está preparando. Intenta otra vez."
        }
        animate.setOnClickListener {
            if (latestScript.isBlank()) statusView.text = "Primero genera un guion."
            else startActivity(android.content.Intent(this, PreviewActivity::class.java)
                .putExtra("topic", topicInput.text.toString()).putExtra("script", latestScript))
        }
        root.addView(label("Estado"))
        statusView = TextView(this).apply {
            text = "Listo para comenzar."; textSize = 14f; setTextColor(Color.rgb(150, 235, 180))
            setPadding(dp(12), dp(12), dp(12), dp(12)); setBackgroundColor(panel)
        }
        root.addView(statusView, matchWrap())
        root.addView(text("Esta versión inicial no exporta MP4; incluye guiones, voz del teléfono y una animación de muestra.", 12f, muted, false, Gravity.START).apply {
            setPadding(0, dp(18), 0, 0)
        })
        scroll.addView(root); setContentView(scroll)
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) tts?.language = Locale("es", "ES")
    }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }

    private fun makeScript(topic: String, minutes: Int): String {
        val parts = max(3, minutes * 3)
        val b = StringBuilder("TÍTULO: $topic\n\nGANCHO\n¿Y si te dijera que entender $topic puede cambiar la forma en que ves el mundo? Quédate, porque Nico te lo explica de manera sencilla.\n\n")
        for (i in 1..parts) b.append("ESCENA $i\nNico presenta el punto $i sobre $topic. Explica una idea concreta con palabras sencillas, añade un ejemplo cotidiano y conecta con la siguiente escena para mantener la curiosidad.\n\n")
        b.append("CIERRE\nNico resume lo más importante e invita al público a comentar y suscribirse.\n")
        return b.toString()
    }
    private fun label(value: String) = text(value, 16f, white, true, Gravity.START).apply { setPadding(0, dp(18), 0, dp(8)) }
    private fun text(value: String, size: Float, color: Int, bold: Boolean, gravity: Int) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); this.gravity = gravity
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }
    private fun button(value: String) = Button(this).apply {
        text = value; textSize = 12f; isAllCaps = false; setTextColor(white); setBackgroundColor(accent)
    }
    private fun matchWrap() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(6) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}

class PreviewActivity : Activity() {
    private var tick = 0
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private lateinit var stickman: StickmanView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val topic = intent.getStringExtra("topic") ?: "Nico"
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(15, 18, 35)); setPadding(16, 20, 16, 16)
        }
        root.addView(TextView(this).apply { text = "ANIMACIÓN DE NICO"; textSize = 20f; setTextColor(Color.WHITE); gravity = Gravity.CENTER })
        root.addView(TextView(this).apply { text = topic; textSize = 14f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER; setPadding(0, 8, 0, 12) })
        stickman = StickmanView(this)
        root.addView(stickman, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply { text = "Volver al estudio"; setOnClickListener { finish() } })
        setContentView(root)
        handler.post(object : Runnable {
            override fun run() { tick++; stickman.frame = tick; stickman.invalidate(); handler.postDelayed(this, 80) }
        })
    }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
}

class StickmanView(context: android.content.Context) : View(context) {
    var frame = 0
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; strokeWidth = 9f; strokeCap = android.graphics.Paint.Cap.ROUND; style = android.graphics.Paint.Style.STROKE
    }
    private val blue = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(91, 115, 255); style = android.graphics.Paint.Style.FILL }
    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas); canvas.drawColor(Color.rgb(24, 29, 51))
        val cx = width / 2f; val cy = height / 2f
        val bob = kotlin.math.sin(frame / 7.0).toFloat() * 7f
        val headY = cy - 95f + bob
        canvas.drawCircle(cx, headY, 34f, paint)
        canvas.drawLine(cx, headY + 35f, cx, cy + 55f + bob, paint)
        val arm = kotlin.math.sin(frame / 5.0).toFloat() * 30f
        canvas.drawLine(cx, cy - 15f + bob, cx - 65f, cy + arm, paint)
        canvas.drawLine(cx, cy - 15f + bob, cx + 65f, cy - arm, paint)
        canvas.drawLine(cx, cy + 55f + bob, cx - 45f, cy + 125f, paint)
        canvas.drawLine(cx, cy + 55f + bob, cx + 45f, cy + 125f, paint)
        canvas.drawCircle(cx, cy + 190f, 5f, blue)
        val label = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 25f; textAlign = android.graphics.Paint.Align.CENTER }
        canvas.drawText("NICO", cx, cy + 235f, label)
    }
}
