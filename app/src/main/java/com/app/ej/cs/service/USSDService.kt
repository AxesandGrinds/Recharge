package com.app.ej.cs.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import java.util.*


open class USSDService : AccessibilityService() {


    val TAG: String = "ATTENTION USSDService"
    private lateinit var phoneNumber: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val extras = intent!!.extras

        if (extras == null) {
            Log.e(TAG, "Extras Are Null")
        }
        else {
            phoneNumber = extras["phoneNumber"] as String
            Log.e(TAG, "Extras Are Not Null: Phone Number: $phoneNumber")
        }

        return super.onStartCommand(intent, flags, startId)

    }

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public override fun onAccessibilityEvent(event: AccessibilityEvent) {

        Log.e(TAG, "onAccessibilityEvent")

        val source: AccessibilityNodeInfo = event.source


        if (source != null) {

            //capture the EditText simply by using FOCUS_INPUT (since the EditText has the focus), you can probably find it with the viewId input_field
            val inputNode = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

            if (inputNode != null) { //prepare you text then fill it using ACTION_SET_TEXT

                val arguments = Bundle()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    arguments.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        phoneNumber)

                    inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                    Log.e(TAG, "Should Paste Phone Number $phoneNumber Now")

                    //"Click" the Send button
                    val list = source.findAccessibilityNodeInfosByText("Send")

                    for (node in list) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }

                    Log.e(TAG, "Should Click Send Button Now")

                }

            }

        }













//        /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
//
//        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.className.toString().contains(
//                "AlertDialog"
//            )) {
//            return;
//        }
//
//        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.className.equals(
//                "android.widget.TextView"
//            ))) {
//            return;
//        }
//
//        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(
//                source.text
//            )) {
//            return;
//        }
//
//        val eventText: List<CharSequence> =
//
//        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            event.text
//        }
//        else {
//            Collections.singletonList(source.text);
//        }
//
//        val text: String? = processUSSDText(eventText)
//
//        if( TextUtils.isEmpty(text) ) return
//
//        // Close dialog
//        performGlobalAction(GLOBAL_ACTION_BACK) // This works on 4.1+ only
//
//        Log.d(TAG, text!!);
//        // Handle USSD response here

    }

    private fun processUSSDText(eventText: List<CharSequence>): String? {

        for (s in eventText) {

            val text: String = s.toString()

            // Return text if text is the expected ussd response

            if( true ) {
                return text
            }

        }

        return null

    }

    public override fun onInterrupt() {}

    protected override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(TAG, "onServiceConnected")

        val info: AccessibilityServiceInfo = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = arrayOf("com.app.ej.cs", "com.android.server.telecom")

        info.eventTypes =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info

    }

    inner class USSDServiceBinder : Binder() {
        fun getService() = this@USSDService
    }

    companion object {
        private const val MIN_DISTANCE_FOR_UPDATE: Long = 10
        private const val MIN_TIME_FOR_UPDATE = 1000 * 60 * 2.toLong()
    }

}


















