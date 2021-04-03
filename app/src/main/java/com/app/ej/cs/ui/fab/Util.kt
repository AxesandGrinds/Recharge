package com.app.ej.cs.ui.fab

import android.content.Context
import android.os.Build
import kotlin.math.roundToInt

internal object Util {

  fun dpToPx(context: Context, dp: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale).roundToInt()
  }

  fun hasJellyBean(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
  }

  fun hasLollipop(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
  }

}
