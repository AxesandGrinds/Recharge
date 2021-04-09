package com.app.ej.cs.ui.permission.fragment

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.ej.cs.R
import com.app.ej.cs.ui.permission.callback.BaseCallback
import com.app.ej.cs.ui.permission.model.PermissionModel
import com.app.ej.cs.ui.permission.utils.FontTypeHelper
import com.app.ej.cs.ui.permission.utils.ThemeUtil;


class PermissionFragment : Fragment(), View.OnClickListener {

    private var permissionModel: PermissionModel? = null
    private var callback: BaseCallback? = null
    private var image: ImageView? = null
    private var message: TextView? = null
    private var previous: ImageButton? = null
    private var request: ImageButton? = null
    private var next: ImageButton? = null
    private var title: TextView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = if (context is BaseCallback) {
            context as BaseCallback
        } else {
            throw IllegalArgumentException("Activity must Implement BaseCallback.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (permissionModel != null) {
            outState.putParcelable(PERMISSION_INSTANCE, permissionModel)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.permissionhelper_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            permissionModel = savedInstanceState.getParcelable(PERMISSION_INSTANCE)
        } else {
            permissionModel = requireArguments().getParcelable(PERMISSION_INSTANCE)
        }
        if (permissionModel == null) {
            throw NullPointerException("Permission Model some how went nuts and become null or was it?.")
        }
        title = view.findViewById<View>(R.id.title) as TextView
        image = view.findViewById<View>(R.id.image) as ImageView
        message = view.findViewById<View>(R.id.message) as TextView
        previous = view.findViewById<View>(R.id.previous) as ImageButton
        next = view.findViewById<View>(R.id.next) as ImageButton
        request = view.findViewById<View>(R.id.request) as ImageButton
        next!!.setOnClickListener(this)
        previous!!.setOnClickListener(this)
        request!!.setOnClickListener(this)
        initViews()
    }

    override fun onClick(v: View) {

        if (v.id == R.id.previous) {

            permissionModel?.permissionName?.let { callback?.onSkip(it) }

        }
        else if (v.id == R.id.next) {

            if (!permissionModel?.isCanSkip!!) {
                permissionModel?.permissionName?.let { callback?.onPermissionRequest(it, false) }
            }
            else {
                permissionModel?.permissionName?.let { callback?.onNext(it) }
            }

        }
        else if (v.id == R.id.request) {

            permissionModel?.permissionName?.let { callback?.onPermissionRequest(it, true) }

        }

    }

    private fun initViews() {

        request?.visibility = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) View.GONE else View.VISIBLE

        permissionModel?.let { image?.setImageResource(it.imageResourceId) }

        title?.text = permissionModel?.title

        if (permissionModel?.textColor === 0) Color.WHITE else permissionModel?.textColor?.let {

            title?.setTextColor(it)

        }

        message?.setText(permissionModel?.message)

        setTextSizes()

        if (permissionModel?.textColor === 0) Color.WHITE else permissionModel?.textColor?.let {

            message!!.setTextColor(it)

        }

        if (permissionModel?.previousIcon === 0) R.drawable.ic_baseline_arrow_back_60

        else permissionModel?.previousIcon?.let { previous?.setImageResource(it) }


        if (permissionModel?.requestIcon === 0) R.drawable.ic_baseline_check_circle_60

        else permissionModel?.requestIcon?.let { request?.setImageResource(it) }


        if (permissionModel?.nextIcon === 0) R.drawable.ic_baseline_arrow_forward_60

        else permissionModel?.nextIcon?.let { next?.setImageResource(it) }


        FontTypeHelper.setTextTypeFace(title!!, permissionModel?.fontType)
        FontTypeHelper.setTextTypeFace(message!!, permissionModel?.fontType)
    }

    private fun setTextSizes() {

        val isFromResources: Boolean =
            permissionModel?.textSize?.let { ThemeUtil.isTextSizeFromResources(requireContext(), it) }!!

        if (isFromResources) {

            permissionModel?.let { resources.getDimension(it?.textSize) }?.let {

                title!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)

            }

            permissionModel?.textSize?.let { resources.getDimension(it) }?.let {

                message!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)

            }

        }
        else {

            permissionModel?.textSize?.toFloat()?.let {

                title!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, it) }

            permissionModel?.textSize?.toFloat()?.let {

                message!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)

            }

        }

    }

    companion object {

        private const val PERMISSION_INSTANCE = "PERMISSION_INSTANCE"
        fun newInstance(permissionModel: PermissionModel?): PermissionFragment {
            val fragment = PermissionFragment()
            val localBundle = Bundle()
            localBundle.putParcelable(PERMISSION_INSTANCE, permissionModel)
            fragment.arguments = localBundle
            return fragment
        }
    }
}
