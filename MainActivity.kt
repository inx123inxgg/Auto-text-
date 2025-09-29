package com.example.autotextinterval

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var btnStartStop: Button
    private lateinit var tvAutoText: TextView
    private lateinit var etNewText: EditText
    private lateinit var btnAddText: Button
    private lateinit var listContainer: LinearLayout
    private lateinit var etInterval: EditText

    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var words = mutableListOf<String>()
    private var intervalSeconds = 2

    private val prefs by lazy { getSharedPreferences("AutoTextPrefs", Context.MODE_PRIVATE) }

    private val updateTextRunnable = object : Runnable {
        override fun run() {
            if (words.isNotEmpty()) {
                val randomText = words[Random.nextInt(words.size)]
                tvAutoText.text = randomText
            }
            if (isRunning) {
                handler.postDelayed(this, (intervalSeconds * 1000).toLong())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartStop = findViewById(R.id.btnStartStop)
        tvAutoText = findViewById(R.id.tvAutoText)
        etNewText = findViewById(R.id.etNewText)
        btnAddText = findViewById(R.id.btnAddText)
        listContainer = findViewById(R.id.listContainer)
        etInterval = findViewById(R.id.etInterval)

        // Load saved list
        val savedSet = prefs.getStringSet("words", setOf())
        words = savedSet?.toMutableList() ?: mutableListOf()
        refreshListUI()

        btnStartStop.setOnClickListener {
            if (isRunning) stopAutoText() else startAutoText()
        }

        btnAddText.setOnClickListener {
            val newWord = etNewText.text.toString().trim()
            if (newWord.isNotEmpty()) {
                words.add(newWord)
                saveWords()
                refreshListUI()
                etNewText.text.clear()
            }
        }
    }

    private fun startAutoText() {
        val input = etInterval.text.toString().trim()
        intervalSeconds = if (input.isNotEmpty()) input.toInt() else 2

        isRunning = true
        btnStartStop.text = "Stop"
        handler.post(updateTextRunnable)
    }

    private fun stopAutoText() {
        isRunning = false
        btnStartStop.text = "Start"
        handler.removeCallbacks(updateTextRunnable)
    }

    private fun refreshListUI() {
        listContainer.removeAllViews()
        for ((index, word) in words.withIndex()) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val tv = TextView(this)
            tv.text = "${index + 1}. $word"
            tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val btnDelete = Button(this)
            btnDelete.text = "X"
            btnDelete.setOnClickListener {
                words.removeAt(index)
                saveWords()
                refreshListUI()
            }

            row.addView(tv)
            row.addView(btnDelete)
            listContainer.addView(row)
        }
    }

    private fun saveWords() {
        prefs.edit().putStringSet("words", words.toSet()).apply()
    }
}
