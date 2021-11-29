package com.ej.recharge.ui.permission.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.*
import androidx.core.app.ActivityCompat
import com.ej.recharge.R
import com.ej.recharge.ui.permission.model.PermissionModel

/// TOO DONE
class PermissionModelBuilder private constructor(private val context: Context) {

  private val permissionModel: PermissionModel

  fun build(): PermissionModel {
    return permissionModel
  }

  fun withPermissionName(permissionName: String): PermissionModelBuilder {
    permissionModel.permissionName = permissionName
    return this
  }

  fun withImageResourceId(@DrawableRes imageResourceId: Int): PermissionModelBuilder {
    permissionModel.imageResourceId = imageResourceId
    return this
  }

  fun withLayoutColor(@ColorInt layoutColor: Int): PermissionModelBuilder {
    permissionModel.layoutColor = layoutColor
    return this
  }

  fun withLayoutColorRes(@ColorRes layoutColor: Int): PermissionModelBuilder {
    permissionModel.layoutColor =
      ActivityCompat.getColor(context, layoutColor)
    return this
  }

  fun withTextColor(@ColorInt textColor: Int): PermissionModelBuilder {
    permissionModel.textColor = textColor
    return this
  }

  fun withTextColorRes(@ColorRes textColor: Int): PermissionModelBuilder {
    permissionModel.textColor =
      ActivityCompat.getColor(context, textColor)
    return this
  }

  fun withTextSize(@DimenRes textSize: Int): PermissionModelBuilder {
    permissionModel.textSize = textSize
    return this
  }

  fun withExplanationMessage(explanationMessage: String): PermissionModelBuilder {
    permissionModel.explanationMessage = explanationMessage
    return this
  }

  fun withExplanationMessage(@StringRes explanationMessage: Int): PermissionModelBuilder {
    permissionModel.explanationMessage = context.getString(explanationMessage)
    return this
  }

  fun withCanSkip(canSkip: Boolean): PermissionModelBuilder {
    permissionModel.isCanSkip = canSkip
    return this
  }

  fun withRequestIcon(@DrawableRes requestIcon: Int): PermissionModelBuilder {
    permissionModel.requestIcon = requestIcon
    return this
  }

  fun withPreviousIcon(@DrawableRes previousIcon: Int): PermissionModelBuilder {
    permissionModel.previousIcon = previousIcon
    return this
  }

  fun withNextIcon(@DrawableRes nextIcon: Int): PermissionModelBuilder {
    permissionModel.nextIcon = nextIcon
    return this
  }

  fun withMessage(message: String): PermissionModelBuilder {
    permissionModel.message = message
    return this
  }

  fun withMessage(@StringRes message: Int): PermissionModelBuilder {
    permissionModel.message = context.getString(message)
    return this
  }

  fun withTitle(title: String): PermissionModelBuilder {
    permissionModel.title = title
    return this
  }

  fun withTitle(@StringRes title: Int): PermissionModelBuilder {
    permissionModel.title = context.getString(title)
    return this
  }

  /**
   * @param fontType
   * ex: (fonts/my_custom_text.ttf);
   */
  fun withFontType(fontType: String): PermissionModelBuilder {
    permissionModel.fontType = fontType
    return this
  }

  companion object {
    fun withContext(context: Context): PermissionModelBuilder {
      return PermissionModelBuilder(context)
    }
  }

  init {

    permissionModel = PermissionModel() // Generate sane default values
    withTextColor(Color.WHITE)
    withTextSize(R.dimen.permissions_text_size)
    withRequestIcon(R.drawable.ic_arrow_done)
    withPreviousIcon(R.drawable.ic_arrow_left)
    withNextIcon(R.drawable.ic_arrow_right)
//    withRequestIcon(R.drawable.ic_baseline_check_circle_60)
//    withPreviousIcon(R.drawable.ic_baseline_arrow_back_60)
//    withNextIcon(R.drawable.ic_baseline_arrow_forward_60)

  }

}
