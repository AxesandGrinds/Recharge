package com.ej.recharge.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.content.res.ResourcesCompat
import com.ej.recharge.R
import com.ej.recharge.repository.entity.UserAndFriendInfo
import com.droidman.ktoasty.KToasty
//import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt


fun Activity.getRootView(): View {
    return findViewById<View>(android.R.id.content)
}
fun Context.convertDpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    )
}
fun Activity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    this.getRootView().getWindowVisibleDisplayFrame(visibleBounds)
    val heightDiff = getRootView().height - visibleBounds.height()
    val marginOfError = this.convertDpToPx(50F).roundToInt()
//    val marginOfError = Math.round(this.convertDpToPx(50F))
    return heightDiff > marginOfError
}

fun Activity.isKeyboardClosed(): Boolean {
    return !this.isKeyboardOpen()
}

val Context.inputService get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val ironSourceAppKey: String = "10b3b2c6d";

class Util {

    public var rechargeCodeTemp: String? = null
    public var pAccountTemp:    String? = null
    public var phoneNumberTemp: String? = null
    public var amountTemp:      String? = null
    public var passwordTemp:    String? = null


    public fun addFontToAppBarTitle(supportActionBar: ActionBar, applicationContext: Context) {

        val actionBar: ActionBar = supportActionBar
        val tv: TextView = TextView(applicationContext)
        val typeface: Typeface = ResourcesCompat.getFont(applicationContext, R.font.dancingscriptvariablefontwght)!!
        val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, // Width of TextView
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ); // Height of TextView
        tv.layoutParams = lp
        tv.text = "Recharge" // ActionBar title text
        tv.textSize = 30f
        tv.setTextColor(Color.parseColor("#e64a19"))
        tv.setTypeface(typeface, Typeface.BOLD)
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.customView = tv

    }

    fun checkIfAllMandatoryExist(userAndFriendInfo: UserAndFriendInfo): Boolean {

        return (userAndFriendInfo.usersList[0].name != null  && userAndFriendInfo.usersList[0].name  != "") &&
                (userAndFriendInfo.usersList[0].email != null && userAndFriendInfo.usersList[0].email != "") &&
                (userAndFriendInfo.usersList[0].phone != null && userAndFriendInfo.usersList[0].phone != "") &&
                (userAndFriendInfo.usersList[0].network != null && userAndFriendInfo.usersList[0].network != "")

    }

    fun onShowMessage(message: String, context: Context) {

        val toast    = KToasty.info(context, message, Toast.LENGTH_SHORT, true)

        toast.show()

    }

    fun onShowMessageLong(message: String, context: Context) {

        val toast    = KToasty.info(context, message, Toast.LENGTH_LONG, true)

        toast.show()

    }

    fun onShowMessage(message: String, context: Context, view: View) {

//        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        val toast    = KToasty.info(context, message, Toast.LENGTH_SHORT, true)

//        snackbar.show()
        toast.show()

    }

    fun onShowMessageSuccess(message: String, context: Context) {

        val toast    = KToasty.success(context, message, Toast.LENGTH_SHORT, true)

        toast.show()

    }

    fun onShowMessageSuccessLong(message: String, context: Context) {

        val toast    = KToasty.success(context, message, Toast.LENGTH_LONG, true)

        toast.show()

    }

    fun onShowMessageSuccess(message: String, context: Context, view: View) {

//        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        val toast    = KToasty.success(context, message, Toast.LENGTH_SHORT, true)

//        snackbar.show()
        toast.show()

    }

    fun onShowErrorMessage(message: String, context: Context) {

        val toast    = KToasty.error(context, message, Toast.LENGTH_SHORT, true)

        toast.show()

    }

    fun onShowErrorMessageLong(message: String, context: Context) {

        val toast    = KToasty.error(context, message, Toast.LENGTH_LONG, true)

        toast.show()

    }

    fun onShowErrorMessage(message: String, context: Context, view: View) {

//        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        val toast    = KToasty.error(context, message, Toast.LENGTH_SHORT, true)

//        snackbar.show()
        toast.show()

    }

    fun menuIconWithText(r: Drawable, title: String): CharSequence {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("    $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

}