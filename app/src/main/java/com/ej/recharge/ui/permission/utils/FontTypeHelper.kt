package com.ej.recharge.ui.permission.utils

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.widget.TextView
import java.util.*

/// TOO DONE
/**
 * Created by Kosh on 28 Aug 2016, 7:32 AM
 */
object FontTypeHelper {
    private val cacheTypefaces: MutableMap<String?, Typeface> = LinkedHashMap()
    operator fun get(c: Context, typeFacePath: String?): Typeface? {
        if (TextUtils.isEmpty(typeFacePath)) return null
        synchronized(cacheTypefaces) {
            if (!cacheTypefaces.containsKey(typeFacePath)) {
                val t = Typeface.createFromAsset(c.assets, typeFacePath)
                cacheTypefaces[typeFacePath] = t
            }
            return cacheTypefaces[typeFacePath]
        }
    }

    fun setTextTypeFace(textView: TextView, typeFacePath: String?) {
        val typeface = FontTypeHelper[textView.context, typeFacePath]
        if (typeface != null) {
            textView.typeface = typeface
        }
    }

    fun clear() {
        cacheTypefaces.clear()
    }
}
