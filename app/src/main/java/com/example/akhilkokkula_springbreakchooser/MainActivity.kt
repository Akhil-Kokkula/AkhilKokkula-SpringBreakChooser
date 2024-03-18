package com.example.akhilkokkula_springbreakchooser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import java.util.Locale
import java.util.Random
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var spanishLoc : MutableList<String>
    private lateinit var frenchLoc: MutableList<String>
    private lateinit var englishLoc: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spanishLoc = mutableListOf()
        spanishLoc.add("40.4168,-3.7038")
        spanishLoc.add("19.4326,-99.1332")

        frenchLoc = mutableListOf()
        frenchLoc.add("48.8566,2.3522")
        frenchLoc.add("50.8476,4.3572")

        englishLoc = mutableListOf()
        englishLoc.add("40.7128,-74.0060")
        englishLoc.add("51.5072,-0.1276")


        val languageCode = HashMap<String, String>()
        languageCode["English"] = "en"
        languageCode["Spanish"] = "es"
        languageCode["French"] = "fr"

        val languageSpinner : Spinner = findViewById(R.id.spinner)
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            }
        }

        val micBtn : ImageButton = findViewById(R.id.imageButton)
        micBtn.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode[languageSpinner.selectedItem.toString()])
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a phrase in the chosen language")

            try {
                speechRecognitionLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, " " + e.message, Toast.LENGTH_SHORT).show()
            }
        }

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val shakeLimit = 10f
        sensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val accel = sqrt((x * x + y * y + z * z).toDouble())
                    if (accel > shakeLimit) {
                        val selectedLanguage = languageSpinner.selectedItem.toString()
                        val translatedHello = when (selectedLanguage) {
                            "Spanish" -> "Hola"
                            "French" -> "Bonjour"
                            else -> "Hello"
                        }
                        textToSpeech.speak(translatedHello, TextToSpeech.QUEUE_FLUSH, null, null)
                        openGoogleMaps(selectedLanguage)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val speechRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK && res.data != null) {
            val data: Intent? = res.data
            val resultText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            val speechToTextDisplay : EditText = findViewById(R.id.editTextText)
            val editableRes = resultText?.let { it -> speechToTextDisplay.editableText.replace(0, speechToTextDisplay.length(), it) }
            speechToTextDisplay.text = editableRes
        }
    }

    private fun openGoogleMaps(selectedLanguage : String) {
        val randomInt = Random().nextInt(2)
        val latLongStr = when (selectedLanguage) {
            "Spanish" -> spanishLoc[randomInt]
            "French" -> frenchLoc[randomInt]
            else -> englishLoc[randomInt]
        }
        val latLongArr = latLongStr.split(",")
        val latitude = latLongArr[0].toDouble()
        val longitude = latLongArr[1].toDouble()
        val uri = "geo:$latitude,$longitude".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            println(e.message)
        }
    }
}


