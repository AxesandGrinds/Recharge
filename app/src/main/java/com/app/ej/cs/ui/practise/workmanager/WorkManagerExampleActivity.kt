package com.app.ej.cs.ui.practise.workmanager

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.*
import com.app.ej.cs.databinding.PractiseActivityThreadHandleMessageBinding
import com.app.ej.cs.ui.practise.DATA_KEY
import com.app.ej.cs.ui.practise.LOG_TAG
import com.app.ej.cs.ui.practise.MESSAGE_KEY

class WorkManagerExampleActivity : AppCompatActivity() {

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
//    private fun runCode() {
//        val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
//        WorkManager.getInstance(applicationContext).enqueue(workRequest)
//    }
    private fun runCode() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
//            .setConstraints(constraints)
//            .build()

        val workRequest = OneTimeWorkRequestBuilder<MyCoroutineWorker>()
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id)
            .observe(this, Observer {

                when (it.state) {

                    WorkInfo.State.SUCCEEDED -> {
                        val result = it.outputData.getString(DATA_KEY)
                        log(result ?: "Null")
                    }
                    WorkInfo.State.RUNNING -> {
                        val progress = it.progress.getString(MESSAGE_KEY)
                        if (progress != null) {
                            log(progress)
                        }
                    }
                    else -> { }

                }

            })

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

}
