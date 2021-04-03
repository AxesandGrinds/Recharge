package com.app.ej.cs.ui.practise.jobintentservice

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.app.ej.cs.databinding.PractiseActivityThreadHandleMessageBinding
import com.app.ej.cs.ui.practise.FILE_CONTENTS_KEY
import com.app.ej.cs.ui.practise.FILE_URL
import com.app.ej.cs.ui.practise.LOG_TAG

class JobIntentServiceExampleActivity : AppCompatActivity() {

    private lateinit var binding: PractiseActivityThreadHandleMessageBinding

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
        val receiver = MyResultReceiver(Handler())
        ExampleJobIntentService.startAction(this, FILE_URL, receiver)
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
        Log.i(LOG_TAG, message)
        binding.logDisplay.append(message + "\n")
        scrollTextToEnd()
    }

    /**
     * Scroll to end. Wrapped in post() function so it's the last thing to happen
     */
    private fun scrollTextToEnd() {
        Handler().post { binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private inner class MyResultReceiver(handler: Handler) :
        ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (resultCode == Activity.RESULT_OK) {
                val fileContents = resultData?.getString(FILE_CONTENTS_KEY) ?: "Null"
                log(fileContents)
            }
        }
    }

}
