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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import com.ej.recharge.vision.GraphicOverlay
import com.ej.recharge.vision.GraphicOverlay.Graphic
import com.google.mlkit.vision.text.Text
import java.util.Arrays
import kotlin.math.max
import kotlin.math.min

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class TextGraphicSelected constructor(overlay: GraphicOverlay?, private val line: Text.Line) :
  Graphic(overlay) {

  private val rectPaint: Paint = Paint()
  private val textPaint: Paint
  private val labelPaint: Paint

  val clientLine: Text.Line = line

  companion object {
    private const val TAG = "TextGraphicSelected"
    private const val TEXT_COLOR = Color.BLACK
    private const val MARKER_COLOR = Color.GREEN
    private const val RECT_COLOR = Color.RED
    private const val TEXT_SIZE = 70.0f
    private const val STROKE_WIDTH = 10.0f
  }

  init {

    rectPaint.color = RECT_COLOR
    rectPaint.style = Paint.Style.STROKE
    rectPaint.strokeWidth = STROKE_WIDTH
    textPaint = Paint()
    textPaint.color = TEXT_COLOR
    textPaint.textSize = TEXT_SIZE
    labelPaint = Paint()
    labelPaint.color = MARKER_COLOR
    labelPaint.style = Paint.Style.FILL
    // Redraw the overlay, as this graphic has been added.
    postInvalidate()

  }

  /** Draws the text block annotations for position, size, and raw value on the supplied canvas.  */
  override fun draw(canvas: Canvas) {

    Log.d(TAG, "Line text is: " + line.text)

    Log.d(TAG, "Line boundingbox is: " + line.boundingBox)

    Log.d(TAG, "Line cornerpoint is: " + Arrays.toString(line.cornerPoints))

    Log.d(TAG, "Line language is: " + line.recognizedLanguage)

    val rect = RectF(line.boundingBox)

    // If the image is flipped, the left will be translated to right, and the right to left.
    val x0 = translateX(rect.left)
    val x1 = translateX(rect.right)

    rect.left = min(x0, x1)
    rect.right = max(x0, x1)
    rect.top = translateY(rect.top)
    rect.bottom = translateY(rect.bottom)

    canvas.drawRect(rect, rectPaint)

    val lineHeight =
      TEXT_SIZE + 2 * STROKE_WIDTH
    val textWidth = textPaint.measureText(line.text)

    canvas.drawRect(
      rect.left - STROKE_WIDTH,
      rect.top - lineHeight,
      rect.left + textWidth + 2 * STROKE_WIDTH,
      rect.top,
      labelPaint)

    // Renders the text at the bottom of the box.
    canvas.drawText(
      line.text,
      rect.left,
      rect.top - STROKE_WIDTH,
      textPaint)

    for (element in line.elements) {

      Log.d(TAG, "Element text is: " + element.text)

      Log.d(TAG, "Element boundingbox is: " + element.boundingBox)

      Log.d(TAG, "Element cornerpoint is: " + Arrays.toString(element.cornerPoints))

      Log.d(TAG, "Element language is: " + element.recognizedLanguage)

    }

  }

}
