package com.app.ej.cs.ui.permission.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes


/// TOO DONE
/**
 * please use [PermissionModelBuilder] for more convince
 */
open class PermissionModel : Parcelable {

  var permissionName: String? = null

  @get:DrawableRes
  var imageResourceId = 0

  @get:ColorInt
  var layoutColor = 0

  @get:ColorInt
  var textColor = 0

  @get:DimenRes
  var textSize = 0
  var explanationMessage: String? = null

  @get:DrawableRes
  var previousIcon = 0

  @get:DrawableRes
  var nextIcon = 0

  @get:DrawableRes
  var requestIcon = 0
  var isCanSkip = false
  var message: String? = null
  var title: String? = null
  var fontType: String? = null

  fun settPermissionName(permissionName: String) {
    this.permissionName = permissionName
  }

  fun settExplanationMessage(explanationMessage: String) {
    this.explanationMessage = explanationMessage
  }

  @JvmName("setMessage1")
  fun setMessage(message: String) {
    this.message = message
  }

  fun settTitle(title: String) {
    this.title = title
  }

  /**
   * @param fontType
   * ex: (fonts/my_custom_text.ttf);
   */
  fun settFontType(fontType: String) {
    this.fontType = fontType
  }

  /**
   * please use [PermissionModelBuilder] for more convince
   */
  @Deprecated("")
  constructor() {
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(permissionName)
    dest.writeInt(imageResourceId)
    dest.writeInt(layoutColor)
    dest.writeInt(textColor)
    dest.writeInt(textSize)
    dest.writeString(explanationMessage)
    dest.writeInt(previousIcon)
    dest.writeInt(nextIcon)
    dest.writeInt(requestIcon)
    dest.writeByte(if (isCanSkip) 1.toByte() else 0.toByte())
    dest.writeString(message)
    dest.writeString(title)
    dest.writeString(fontType)
  }

  protected constructor(rin: Parcel) {
    permissionName = rin.readString()
    imageResourceId = rin.readInt()
    layoutColor = rin.readInt()
    textColor = rin.readInt()
    textSize = rin.readInt()
    explanationMessage = rin.readString()
    previousIcon = rin.readInt()
    nextIcon = rin.readInt()
    requestIcon = rin.readInt()
    isCanSkip = rin.readByte().toInt() != 0
    message = rin.readString()
    title = rin.readString()
    fontType = rin.readString()
  }

  companion object {

    @JvmField val CREATOR: Parcelable.Creator<PermissionModel?> =
      object : Parcelable.Creator<PermissionModel?> {
        override fun createFromParcel(source: Parcel): PermissionModel? {
          return PermissionModel(source)
        }

        override fun newArray(size: Int): Array<PermissionModel?> {
          return arrayOfNulls(size)
        }
      }
  }
}
