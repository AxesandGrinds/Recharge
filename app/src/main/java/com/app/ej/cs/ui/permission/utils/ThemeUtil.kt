package com.app.ej.cs.ui.permission.utils


import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.app.ej.cs.R


/// TOO DONE
/**
 * Created by Kosh on 20/11/15 10:58 PM. copyrights @ Innov8tif
 */
object ThemeUtil {
    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
        return value.data
    }

    @ColorInt
    fun getThemeAccentColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, value, true)
        return value.data
    }

    fun isTextSizeFromResources(context: Context, resId: Int): Boolean {
        try {
            return context.resources.getDimension(resId) != 0f
        } catch (ignored: Exception) {
        }
        return false
    }
}
