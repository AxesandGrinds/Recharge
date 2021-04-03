package com.app.ej.cs.ui.practise.jobintentservice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.core.app.JobIntentService
import com.app.ej.cs.ui.practise.FILE_CONTENTS_KEY
import com.app.ej.cs.ui.practise.LOG_TAG
import com.app.ej.cs.ui.practise.RECEIVER_KEY
import java.net.URL
import java.nio.charset.Charset

private const val EXTRA_FILE_URL = "com.example.androidconcurrency2020.extra.FILE_URL"

private const val JOB_ACTION = "com.example.androidconcurrency2020.extra.JOB_ACTION"
private const val JOB_ID = 1001

class ExampleJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        if (intent.action == JOB_ACTION) {
            val url = URL(intent.getStringExtra(EXTRA_FILE_URL))
            val contents = url.readText(Charset.defaultCharset())
            Log.i(LOG_TAG, contents)

            val bundle = Bundle()
            bundle.putString(FILE_CONTENTS_KEY, contents)
            val receiver = intent.getParcelableExtra<ResultReceiver>(RECEIVER_KEY)
            receiver?.send(Activity.RESULT_OK, bundle)
        }
    }

    companion object {
        fun startAction(context: Context, fileUrl: String, receiver: ResultReceiver) {
            val intent = Intent(context, ExampleJobIntentService::class.java).apply {
                action = JOB_ACTION
                putExtra(RECEIVER_KEY, receiver)
                putExtra(EXTRA_FILE_URL, fileUrl)
            }
            enqueueWork(context, ExampleJobIntentService::class.java, JOB_ID, intent)
        }
    }
}
