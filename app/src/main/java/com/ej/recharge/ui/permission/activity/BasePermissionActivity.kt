package com.ej.recharge.ui.permission.activity

import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

import com.ej.recharge.R
import com.ej.recharge.ui.permission.PermissionHelper
import com.ej.recharge.ui.permission.callback.BaseCallback
import com.ej.recharge.ui.permission.callback.OnPermissionCallback
import com.ej.recharge.ui.permission.model.PermissionModel
import com.ej.recharge.ui.permission.utils.ThemeUtil
import com.ej.recharge.ui.permission.widget.CirclePageIndicator
import com.ej.recharge.ui.permission.fragment.PermissionFragment
import com.ej.recharge.ui.permission.utils.FontTypeHelper
import com.ej.recharge.ui.permission.adapter.PagerAdapter



abstract class BasePermissionActivity : AppCompatActivity(),
    OnPermissionCallback, BaseCallback {
    protected var permissionHelper: PermissionHelper? = null
    protected var pager: ViewPager? = null
    protected var indicator // take control to change the color and stuff.
            : CirclePageIndicator? = null
    private var systemOverRequestNumber = 0 /* only show the explanation once otherwise infinite
                                                        LOOP if canSkip is false */
    protected abstract fun permissions(): List<PermissionModel>
    @StyleRes
    protected abstract fun theme(): Int

    /**
     * Intro has finished.
     */
    protected abstract fun onIntroFinished()
    protected abstract fun pagerTransformer(): androidx.viewpager.widget.ViewPager.PageTransformer?
    protected abstract fun backPressIsEnabled(): Boolean

    /**
     * used to notify you that the permission is permanently denied. so you can decide whats next!
     */
    protected abstract fun permissionIsPermanentlyDenied(permissionName: String)

    /**
     * used to notify that the user ignored the permission
     *
     *
     * note: if the [PermissionModel.isCanSkip] return false, we could display the explanation immediately.
     */
    protected abstract fun onUserDeclinePermission(permissionName: String)
    protected override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (pager != null) {
            outState.putInt(PAGER_POSITION, pager!!.getCurrentItem())
        }
        outState.putInt(SYSTEM_OVERLAY_NUM_INSTANCE, systemOverRequestNumber)
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        if (theme() != 0) setTheme(theme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_permissionhelper_layout)
        if (permissions().isEmpty()) {
            onIntroFinished()
            return
        }
        pager = findViewById<android.view.View>(R.id.pager) as ViewPager?
        indicator = findViewById<android.view.View>(R.id.indicator) as CirclePageIndicator?
        pager?.adapter = PagerAdapter(supportFragmentManager, permissions())
        pager?.let { indicator?.setViewPager(it) }
        pager?.offscreenPageLimit = permissions().size
        permissionHelper = PermissionHelper.getInstance(this)
        var color: Int = permissions()[0].layoutColor
        if (color == 0) {
            color = ThemeUtil.getPrimaryColor(this)
        }
        pager?.setBackgroundColor(color)
        onStatusBarColorChange(color)
        pager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                var color: Int? = getPermission(position)?.layoutColor
                if (color == 0) {
                    color = ThemeUtil.getPrimaryColor(this@BasePermissionActivity)
                }
                color?.let { animateColorChange(pager, it) }
            }
        })
        pager?.setPageTransformer(
            true,
            if (pagerTransformer() == null) IntroTransformer() else pagerTransformer()
        )
        if (savedInstanceState != null) {
            pager?.setCurrentItem(savedInstanceState.getInt(PAGER_POSITION), true)
            systemOverRequestNumber = savedInstanceState.getInt(SYSTEM_OVERLAY_NUM_INSTANCE)
        }
    }

    /**
     * Used to determine if the user accepted [android.Manifest.permission.SYSTEM_ALERT_WINDOW] or no.
     *
     *
     * if you never passed the permission this method won't be called.
     */
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        permissionHelper?.onActivityForResult(requestCode)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (backPressIsEnabled()) {
            super.onBackPressed()
        }
    }

    protected override fun onDestroy() {
        super.onDestroy()
        FontTypeHelper.clear()
    }

    override fun onPermissionGranted(permissionName: Array<String?>) { // @NonNull String @NonNull [] permissionName
        onNext(permissionName[0]!!) // we are certain that one permission is requested.
    }

    override fun onPermissionDeclined(permissionName: Array<String>) {
        val model: PermissionModel? = pager?.let { getPermission(it.currentItem) }
        if (model != null) {
            if (!model.isCanSkip) {
                if (!model.permissionName
                        .equals(android.Manifest.permission.SYSTEM_ALERT_WINDOW, true)
                ) {
                    requestPermission(model) //ask again. you asked for it, and i'm just doing it.
                    return
                } else {
                    if (systemOverRequestNumber == 0) { //because boolean is too mainstream. (jk).
                        requestPermission(model)
                        systemOverRequestNumber = 1
                        return
                    } else {
                        model.permissionName?.let { onUserDeclinePermission(it) }
                    }
                }
            } else {
                onUserDeclinePermission(permissionName[0])
            }
        }
        onNext(permissionName[0])
    }

    override fun onPermissionPreGranted(permissionsName: String) {
        onNext(permissionsName)
    }

    override fun onPermissionNeedExplanation(permissionName: String) {
        if (!permissionName.equals(
                android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                ignoreCase = true
            )
        ) {
            val model: PermissionModel? = pager?.let { getPermission(it.getCurrentItem()) }
            if (model != null) {
                requestPermission(model)
            } else { // it will never occur. but in case it does, call it :).
                permissionHelper?.requestAfterExplanation(permissionName)
            }
        } else {
            onPermissionReallyDeclined(permissionName) // sorry, i can't do that, its just bad.
        }
    }

    override fun onPermissionReallyDeclined(permissionName: String) {
        permissionIsPermanentlyDenied(permissionName)
        onNoPermissionNeeded()
    }

    override fun onNoPermissionNeeded() {
        if (pager?.adapter?.count!! - 1 == pager?.currentItem) {
            onIntroFinished()
        } else {
            onNext("") // the irony. I can't pass null too :p.
        }
    }

    override fun onStatusBarColorChange(@ColorInt color: Int) {
        if (color == 0) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cl = 0.9f
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(color, hsv)
            hsv[2] *= cl
            val primaryDark: Int = android.graphics.Color.HSVToColor(hsv)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = primaryDark
        }
    }

    override fun onSkip(permissionName: String) {
        pager?.setCurrentItem(pager!!.getCurrentItem() - 1, true)
    }

    override fun onNext(permissionName: String) {
        var currentPosition: Int? = pager?.currentItem
        if (pager?.adapter?.count!! - 1 == currentPosition) {
            onNoPermissionNeeded()
        } else {
            currentPosition = pager!!.currentItem + 1
            pager!!.setCurrentItem(currentPosition, true)
        }
    }

    override fun onPermissionRequest(permissionName: String, canSkip: Boolean) {
        if (permissionHelper!!.isExplanationNeeded(permissionName)) {
            onPermissionNeedExplanation(permissionName)
        } else {
            permissionHelper!!.request(permissionName)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        permissionHelper!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * @return instance of [PermissionFragment]
     */
    protected fun getFragment(index: Int): PermissionFragment {
        return pager?.let { pager!!.adapter?.instantiateItem(it, index) } as PermissionFragment
    }

    /**
     * return PermissionModel at specific index.
     *
     *
     * if index > [.permissions] null will be returned
     */
    protected fun getPermission(index: Int): PermissionModel? {
        if (permissions().isEmpty()) return null
        return if (index <= permissions().size) { // avoid accessing index does not exists.
            permissions()[index]
        } else null
    }

    /**
     * internal usage to show dialog with explanation you provided and a button to ask the user to request the permission
     */
    protected fun requestPermission(model: PermissionModel) {

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(model.title)
            .setMessage(model.explanationMessage)
            .setPositiveButton(
                "Request",
                DialogInterface.OnClickListener
             { dialog, which ->
                if (model.permissionName
                        .equals(android.Manifest.permission.SYSTEM_ALERT_WINDOW, true)
                ) {
                    permissionHelper?.requestSystemAlertPermission()
                } else {
                    model.permissionName?.let { permissionHelper?.requestAfterExplanation(it) }
                }
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()

    }

    private fun animateColorChange(view: android.view.View?, color: Int) {
        val animator = ValueAnimator()
        animator.setIntValues((view?.background as ColorDrawable).color, color)
        animator.setEvaluator(android.animation.ArgbEvaluator())
        animator.setDuration(600)
        animator.addUpdateListener { animation ->
            if (view != null) {
                view.setBackgroundColor(animation.animatedValue as Int)
            }
            onStatusBarColorChange(animation.animatedValue as Int)
        }
        animator.start()
    }

    protected class IntroTransformer : androidx.viewpager.widget.ViewPager.PageTransformer {
        private fun setAlpha(view: android.view.View, value: Float) {
            view.animate().alpha(value)
        }

        private fun setTranslationX(view: android.view.View, value: Float) {
            view.animate().translationX(value)
        }

        override fun transformPage(view: android.view.View, position: Float) {
            val pageWidth: Int = view.getWidth()
            val message: android.view.View = view.findViewById<android.view.View>(R.id.message)
            val title: android.view.View = view.findViewById<android.view.View>(R.id.title)
            if (position >= -1) {
                if (position <= 0) {
                    setTranslationX(view, -position)
                    setTranslationX(message, pageWidth * position)
                    setTranslationX(title, pageWidth * position)
                    setAlpha(message, 1 + position)
                    setAlpha(title, 1 + position)
                } else if (position <= 1) { // (0,1]
                    setTranslationX(view, position)
                    setTranslationX(message, pageWidth * position)
                    setTranslationX(title, pageWidth * position)
                    setAlpha(message, 1 - position)
                    setAlpha(title, 1 - position)
                }
            }
        }
    }

    companion object {
        private const val PAGER_POSITION = "PAGER_POSITION"
        private const val SYSTEM_OVERLAY_NUM_INSTANCE = "SYSTEM_OVERLAY_NUM_INSTANCE"
    }
}

