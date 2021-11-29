/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ej.recharge.vision.textdetector

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.ej.recharge.vision.GraphicOverlay
import com.ej.recharge.vision.VisionProcessorBase
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** Processor for the text detector demo.  */
class TextRecognitionProcessor(context: Context, textRecognizerOptions: TextRecognizerOptionsInterface) : VisionProcessorBase<Text>(context) {

  private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

  var graphicOverlay: GraphicOverlay? = null

  var canAdd: Boolean = true

  override fun stop() {
    super.stop()
    textRecognizer.close()
  }

  override fun detectInImage(image: InputImage): Task<Text>? {

    return if (canAdd) textRecognizer.process(image) else textRecognizer.process(image)

  }

  fun startRecognition() {
    canAdd = true
  }

  fun stopRecognition() {
    textRecognizer.close()
    graphicOverlay?.clear()
    canAdd = false
    colorCountDown()
  }

  private fun colorCountDown() {

    Handler().postDelayed({ // count down timer start

      object : CountDownTimer(3000, 25) {

        override fun onTick(millisUntilFinished: Long) {
          textRecognizer.close()
          graphicOverlay?.clear()
          canAdd = false
        }

        override fun onFinish() {
          textRecognizer.close()
          graphicOverlay?.clear()
          canAdd = false
        }

      }.start()

    }, 0)

  }

  override fun onSuccess(text: Text, graphicOverlay: GraphicOverlay) {

    if (canAdd) {
      this.graphicOverlay = graphicOverlay
      this.graphicOverlay!!.add(TextGraphicUnselected(this.graphicOverlay, text))
    }
    else {
      textRecognizer.close()
      graphicOverlay.clear()
      this.graphicOverlay?.clear()
      colorCountDown()
    }


    Log.d(TAG, "On-device Text detection successful")

//    logExtrasForTesting(text)

//    graphicOverlay.add(TextGraphicUnselected(graphicOverlay, text))

  }

  override fun onFailure(e: Exception) {
    Log.w(TAG, "Text detection failed.$e")
  }

  companion object {

    private const val TAG = "TextRecProcessor"

    private fun logExtrasForTesting(text: Text?) {

      if (text != null) {

        Log.v(MANUAL_TESTING_LOG, "Detected text has : " + text.textBlocks.size + " blocks")

        for (i in text.textBlocks.indices) {

          val lines = text.textBlocks[i].lines

          Log.v(MANUAL_TESTING_LOG, String.format("Detected text block %d has %d lines", i, lines.size))

          for (j in lines.indices) {

            val elements = lines[j].elements

            Log.v(MANUAL_TESTING_LOG, String.format("Detected text line %d has %d elements", j, elements.size))

            for (k in elements.indices) {

              val element = elements[k]

              Log.v(MANUAL_TESTING_LOG, String.format("Detected text element %d says: %s", k, element.text))

              Log.v(MANUAL_TESTING_LOG,
                String.format(
                  "Detected text element %d has a bounding box: %s",
                  k, element.boundingBox!!.flattenToString()))

              Log.v(MANUAL_TESTING_LOG,
                String.format("Expected corner point size is 4, get %d", element.cornerPoints!!.size))

              for (point in element.cornerPoints!!) {

                Log.v(MANUAL_TESTING_LOG,
                  String.format(
                    "Corner point for element %d is located at: x - %d, y = %d",
                    k, point.x, point.y))

              }

            }

          }

        }

      }

    }

  }

}
