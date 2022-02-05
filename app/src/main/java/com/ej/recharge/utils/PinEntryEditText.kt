package com.ej.recharge.utils


import android.R
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText

class PinEntryEditText : AppCompatEditText {
    private var mSpace = 24f //24 dp by default, space between the lines
    private var mCharSize = 0f
    private var mNumChars = 6f
    private var mLineSpacing = 8f //8dp by default, height of the text from our lines
    private var mMaxLength = 6
    private var mClickListener: OnClickListener? = null
    private var mLineStroke = 1f //1dp by default
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLinesPaint: Paint? = null
    var mStates = arrayOf(
        intArrayOf(R.attr.state_selected),
        intArrayOf(R.attr.state_focused),
        intArrayOf(-R.attr.state_focused)
    )
    var mColors = intArrayOf(
        Color.GREEN,
        Color.BLACK,
        Color.parseColor("#e64a19"),
    )
    var mColorStates = ColorStateList(mStates, mColors)

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
//        context,
//        attrs,
//        defStyleAttr,
//        defStyleRes
//    ) {
//        init(context, attrs)
//    }

    private fun init(context: Context, attrs: AttributeSet) {
        val multi = context.resources.displayMetrics.density
        mLineStroke = multi * mLineStroke
        mLineStrokeSelected = multi * mLineStrokeSelected
        paint.color = Color.parseColor("#e64a19")
        mLinesPaint = Paint(paint)
        mLinesPaint!!.strokeWidth = mLineStroke
        if (!isInEditMode) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                R.attr.colorControlActivated,
                outValue, true
            )
            val colorActivated = outValue.data
            mColors[0] = colorActivated
            context.theme.resolveAttribute(
                R.attr.colorPrimaryDark,
                outValue, true
            )
            val colorDark = outValue.data
            mColors[1] = colorDark
            context.theme.resolveAttribute(
                R.attr.colorControlHighlight,
                outValue, true
            )
            val colorHighlight = outValue.data
            mColors[2] = Color.parseColor("#e64a19")
        }
        setBackgroundResource(0)
        mSpace = multi * mSpace //convert to pixels for our density
        mLineSpacing = multi * mLineSpacing //convert to pixels for our density
        mMaxLength = attrs.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 4)
        mNumChars = mMaxLength.toFloat()

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        })
        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            text?.let { setSelection(it.length) }
            if (mClickListener != null) {
                mClickListener!!.onClick(v)
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas);
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }
        var startX = paddingLeft
        val bottom = height - paddingBottom

        //Text Width
        val text = text
        val textLength = text!!.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(getText(), 0, textLength, textWidths)
        var i = 0
        while (i < mNumChars) {
            updateColorForLines(i == textLength)
            canvas.drawLine(
                startX.toFloat(), bottom.toFloat(), startX + mCharSize, bottom.toFloat(),
                mLinesPaint!!
            )
            if (getText()?.length!! > i) {
                val middle = startX + mCharSize / 2
                if (text != null) {
                    canvas.drawText(
                        text, i, i + 1, middle - textWidths[0] / 2, bottom - mLineSpacing,
                        paint
                    )
                }
            }
            startX += if (mSpace < 0) {
                (mCharSize * 2).toInt()
            } else {
                (mCharSize + mSpace).toInt()
            }
            i++
        }
    }

    private fun getColorForState(vararg states: Int): Int {
        return mColorStates.getColorForState(states, Color.parseColor("#e64a19"))
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean) {
        if (isFocused) {
            mLinesPaint!!.strokeWidth = mLineStrokeSelected
            mLinesPaint!!.color = getColorForState(R.attr.state_focused)
            if (next) {
                mLinesPaint!!.color = getColorForState(R.attr.state_selected)
            }
        } else {
            mLinesPaint!!.strokeWidth = mLineStroke
            mLinesPaint!!.color = getColorForState(-R.attr.state_focused)
        }
    }

    companion object {
        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }
}