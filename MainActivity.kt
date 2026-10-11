package com.jigar.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var chat: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech
    private val audioRequest = 44

    // Replace with your deployed HTTPS backend URL. Do not put API keys in this app.
    private val backendUrl = "https://YOUR-BACKEND-DOMAIN.example"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildUi()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), audioRequest)
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 36, 24, 24)
            setBackgroundColor(android.graphics.Color.rgb(13, 19, 39))
        }
        fun label(value: String, size: Float) = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 8, 0, 8)
        }
        root.addView(label("JIGAR AI", 30f))
        root.addView(label("हिंदी AI चैट • आवाज़ • फोटो बनाने का अनुरोध", 15f))
        status = label("तैयार हूँ — नीचे लिखें या माइक दबाएँ।", 14f)
        root.addView(status)
        chat = label("JIGAR AI: नमस्ते! मैं आपकी क्या मदद करूँ?", 17f)
        root.addView(chat, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        input = EditText(this).apply {
            hint = "अपना सवाल या फोटो का विवरण लिखें…"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.LTGRAY)
        }
        root.addView(input)

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(Button(this).apply {
            text = "भेजें"
            setOnClickListener {
                val q = input.text.toString().trim()
                if (q.isNotEmpty()) sendChat(q)
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row1.addView(Button(this).apply {
            text = "🎙️ बोलें"
            setOnClickListener { listen() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(row1)

        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(Button(this).apply {
            text = "फोटो बनाएँ"
            setOnClickListener {
                val prompt = input.text.toString().trim()
                if (prompt.isBlank()) showReply("पहले फोटो कैसी चाहिए, उसका विवरण लिखें।")
                else generateImage(prompt)
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row2.addView(Button(this).apply {
            text = "Google खोज"
            setOnClickListener {
                val q = input.text.toString().trim().ifBlank { "आज की खबरें" }
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(q))))
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(row2)
        root.addView(Button(this).apply {
            text = "मौसम खोजें"
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode("आज का मौसम"))))
            }
        })
        setContentView(root)
    }

    private fun listen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), audioRequest)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showReply("इस फोन पर speech recognition उपलब्ध नहीं है।")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JIGAR AI से हिंदी में बात करें")
        }
        try {
            startActivityForResult(intent, 2635)
        } catch (_: Exception) {
            showReply("माइक शुरू नहीं हो पाया। कृपया फिर कोशिश करें।")
        }
    }

    @Deprecated("Uses Android's built-in speech recognition activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2635 && resultCode == RESULT_OK) {
            val words = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            input.setText(words)
            if (words.isNotBlank()) {
                val low = words.lowercase(Locale.ROOT)
                when {
                    low.contains("youtube") && (low.contains("खोल") || low.contains("open")) -> openApp("com.google.android.youtube", "YouTube ऐप नहीं मिला।")
                    low.contains("chrome") && (low.contains("खोल") || low.contains("open")) -> openApp("com.android.chrome", "Chrome ऐप नहीं मिला।")
                    low.contains("hey jigar") || low.contains("हे जिगर") -> showReply("जी, बताइए! अपना सवाल बोलें या लिखें।")
                    low.contains("मौसम") || low.contains("weather") -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode("आज का मौसम"))))
                        showReply("मौसम की जानकारी खोजने के लिए ब्राउज़र खोल दिया है।")
                    }
                    else -> sendChat(words)
                }
            }
        }
    }

    private fun openApp(packageName: String, error: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) startActivity(launch) else showReply(error)
    }

    private fun sendChat(question: String) = postJson("/chat", JSONObject().put("message", question), "reply")
    private fun generateImage(prompt: String) = postJson("/image", JSONObject().put("prompt", prompt), "imageUrl")

    private fun postJson(path: String, payload: JSONObject, resultKey: String) {
        if (backendUrl.contains("YOUR-BACKEND-DOMAIN")) {
            showReply("पहले README में दिए निर्देश से backend deploy करें और ऐप में उसका HTTPS URL डालें।")
            return
        }
        status.text = "कृपया इंतज़ार करें…"
        thread {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(backendUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 60000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
                val body = stream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                if (conn.responseCode !in 200..299) throw IllegalStateException(json.optString("error", "Server error"))
                val result = json.optString(resultKey, "")
                runOnUiThread {
                    if (resultKey == "imageUrl" && result.isNotBlank()) {
                        showReply("फोटो बन गई है। इसे ब्राउज़र में खोलने के लिए नीचे लिंक दबाएँ:\n$result")
                        val open = Button(this).apply {
                            text = "फोटो खोलें"
                            setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result))) }
                        }
                        (chat.parent as? LinearLayout)?.addView(open)
                    } else showReply(result.ifBlank { "जवाब नहीं मिला।" })
                }
            } catch (e: Exception) {
                runOnUiThread { showReply("कनेक्शन या API में समस्या: ${e.message ?: "कृपया बाद में कोशिश करें"}") }
            } finally {
                conn?.disconnect()
            }
        }
    }

    private fun showReply(message: String) {
        status.text = "तैयार हूँ"
        chat.text = "JIGAR AI: $message"
        if (::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jigar_reply")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale("en", "IN")
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) tts.shutdown()
        super.onDestroy()
    }
}
