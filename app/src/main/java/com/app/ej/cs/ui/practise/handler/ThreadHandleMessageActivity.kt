package com.app.ej.cs.ui.practise.handler

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.app.ej.cs.databinding.PractiseActivityThreadHandleMessageBinding
import kotlin.concurrent.thread

const val MESSAGE_KEY = "message_key"

class ThreadHandleMessageActivity : AppCompatActivity() {

    private lateinit var binding: PractiseActivityThreadHandleMessageBinding
    private val TAG: String? = ThreadHandleMessageActivity::class.simpleName

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            val message = bundle?.getString(MESSAGE_KEY)
            log(message ?: "message was null")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding for view object references
        binding = PractiseActivityThreadHandleMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize button click handlers
        with(binding) {
            runButton.setOnClickListener { runCode() }
            clearButton.setOnClickListener { clearOutput() }
        }

    }

    /**
     * Run some code
     */
    private fun runCode() {
        thread(start = true) {
            val bundle = Bundle()
            for (i in 1..10) {
                bundle.putString(MESSAGE_KEY, "Looping $i")
                Message().also {
                    it.data = bundle
                    handler.sendMessage(it)
                }
                Thread.sleep(1000)
            }
            bundle.putString(MESSAGE_KEY, "All done!")
            Message().also {
                it.data = bundle
                handler.sendMessage(it)
            }
        }
    }

    /**
     * Clear log display
     */
    private fun clearOutput() {
        binding.logDisplay.text = ""
        scrollTextToEnd()
    }

    /**
     * Log output to logcat and the screen
     */
    @Suppress("SameParameterValue")
    private fun log(message: String) {
        Log.i(TAG, message)
        binding.logDisplay.append(message + "\n")
        scrollTextToEnd()
    }

    /**
     * Scroll to end. Wrapped in post() function so it's the last thing to happen
     */
    private fun scrollTextToEnd() {
        Handler().post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

}
