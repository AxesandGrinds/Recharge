package com.app.ej.cs.ui.fab

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import com.app.ej.cs.R

@SuppressLint("AppCompatCustomView")
class FloatingActionButton : ImageButton {
  var mFabSize = 0
  var mShowShadow = false
  var mShadowColor = 0
  var mShadowRadius = Util.dpToPx(context, 4f)
  var mShadowXOffset = Util.dpToPx(context, 1f)
  var mShadowYOffset = Util.dpToPx(context, 3f)
  private var mColorNormal = 0
  private var mColorPressed = 0
  private var mColorDisabled = 0
  private var mColorRipple = 0
  private var mIcon: Drawable? = null
  private val mIconSize = Util.dpToPx(context, 24f)
  var showAnimation: Animation? = null
  var hideAnimation: Animation? = null
  private var mLabelText: String? = null
  private var mClickListener: OnClickListener? = null
  private var mBackgroundDrawable: Drawable? = null
  private var mUsingElevation = false
  private var mUsingElevationCompat = false

  // Progress
  private var mProgressBarEnabled = false
  private var mProgressWidth = Util.dpToPx(context, 6f)
  private var mProgressColor = 0
  private var mProgressBackgroundColor = 0
  private var mShouldUpdateButtonPosition = false
  private var mOriginalX = -1f
  private var mOriginalY = -1f
  private var mButtonPositionSaved = false
  private var mProgressCircleBounds = RectF()
  private val mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var mProgressIndeterminate = false
  private var mLastTimeAnimated: Long = 0
  private var mSpinSpeed = 195.0f //The amount of degrees per second
  private var mPausedTimeWithoutGrowing: Long = 0
  private var mTimeStartGrowing = 0.0
  private var mBarGrowingFromFront = true
  private val mBarLength = 16
  private var mBarExtraLength = 0f
  private var mCurrentProgress = 0f
  private var mTargetProgress = 0f
  private var mProgress = 0
  private var mAnimateProgress = false
  private var mShouldProgressIndeterminate = false
  private var mShouldSetProgress = false

  @get:Synchronized
  @set:Synchronized
  var max = 100

  @get:Synchronized
  var isProgressBackgroundShown = false
    private set

  @JvmOverloads
  constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    init(context, attrs, defStyleAttr)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
  ) {
    init(context, attrs, defStyleAttr)
  }

  private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
    val attr =
      context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0)
    mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, -0x25bcca)
    mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, -0x18afbd)
    mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, -0x555556)
    mColorRipple = attr.getColor(R.styleable.FloatingActionButton_fab_colorRipple, -0x66000001)
    mShowShadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true)
    mShadowColor = attr.getColor(R.styleable.FloatingActionButton_fab_shadowColor, 0x66000000)
    mShadowRadius =
      attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowRadius, mShadowRadius)
    mShadowXOffset =
      attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowXOffset, mShadowXOffset)
    mShadowYOffset =
      attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowYOffset, mShadowYOffset)
    mFabSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL)
    mLabelText = attr.getString(R.styleable.FloatingActionButton_fab_label)
    mShouldProgressIndeterminate =
      attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_indeterminate, false)
    mProgressColor = attr.getColor(R.styleable.FloatingActionButton_fab_progress_color, -0xff6978)
    mProgressBackgroundColor =
      attr.getColor(R.styleable.FloatingActionButton_fab_progress_backgroundColor, 0x4D000000)
    max = attr.getInt(R.styleable.FloatingActionButton_fab_progress_max, max)
    isProgressBackgroundShown =
      attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_showBackground, true)
    if (attr.hasValue(R.styleable.FloatingActionButton_fab_progress)) {
      mProgress = attr.getInt(R.styleable.FloatingActionButton_fab_progress, 0)
      mShouldSetProgress = true
    }
    if (attr.hasValue(R.styleable.FloatingActionButton_fab_elevationCompat)) {
      val elevation =
        attr.getDimensionPixelOffset(R.styleable.FloatingActionButton_fab_elevationCompat, 0)
          .toFloat()
      if (isInEditMode) {
        setElevation(elevation)
      } else {
        setElevationCompat(elevation)
      }
    }
    initShowAnimation(attr)
    initHideAnimation(attr)
    attr.recycle()
    if (isInEditMode) {
      if (mShouldProgressIndeterminate) {
        setIndeterminate(true)
      } else if (mShouldSetProgress) {
        saveButtonOriginalPosition()
        setProgress(mProgress, false)
      }
    }

//        updateBackground();
    isClickable = true
  }

  private fun initShowAnimation(attr: TypedArray) {
    val resourceId =
      attr.getResourceId(R.styleable.FloatingActionButton_fab_showAnimation, R.anim.fab_scale_up)
    showAnimation = AnimationUtils.loadAnimation(context, resourceId)
  }

  private fun initHideAnimation(attr: TypedArray) {
    val resourceId =
      attr.getResourceId(R.styleable.FloatingActionButton_fab_hideAnimation, R.anim.fab_scale_down)
    hideAnimation = AnimationUtils.loadAnimation(context, resourceId)
  }

  private val circleSize: Int
    private get() = resources.getDimensionPixelSize(if (mFabSize == SIZE_NORMAL) R.dimen.fab_size_normal else R.dimen.fab_size_mini)

  private fun calculateMeasuredWidth(): Int {
    var width = circleSize + calculateShadowWidth()
    if (mProgressBarEnabled) {
      width += mProgressWidth * 2
    }
    return width
  }

  private fun calculateMeasuredHeight(): Int {
    var height = circleSize + calculateShadowHeight()
    if (mProgressBarEnabled) {
      height += mProgressWidth * 2
    }
    return height
  }

  fun calculateShadowWidth(): Int {
    return if (hasShadow()) shadowX * 2 else 0
  }

  fun calculateShadowHeight(): Int {
    return if (hasShadow()) shadowY * 2 else 0
  }

  private val shadowX: Int
    private get() = mShadowRadius + Math.abs(mShadowXOffset)
  private val shadowY: Int
    private get() = mShadowRadius + Math.abs(mShadowYOffset)

  private fun calculateCenterX(): Float {
    return (measuredWidth / 2).toFloat()
  }

  private fun calculateCenterY(): Float {
    return (measuredHeight / 2).toFloat()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight())
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (mProgressBarEnabled) {
      if (isProgressBackgroundShown) {
        canvas.drawArc(mProgressCircleBounds, 360f, 360f, false, mBackgroundPaint)
      }
      var shouldInvalidate = false
      if (mProgressIndeterminate) {
        shouldInvalidate = true
        val deltaTime = SystemClock.uptimeMillis() - mLastTimeAnimated
        val deltaNormalized = deltaTime * mSpinSpeed / 1000.0f
        updateProgressLength(deltaTime)
        mCurrentProgress += deltaNormalized
        if (mCurrentProgress > 360f) {
          mCurrentProgress -= 360f
        }
        mLastTimeAnimated = SystemClock.uptimeMillis()
        var from = mCurrentProgress - 90
        var to = mBarLength + mBarExtraLength
        if (isInEditMode) {
          from = 0f
          to = 135f
        }
        canvas.drawArc(mProgressCircleBounds, from, to, false, mProgressPaint)
      } else {
        if (mCurrentProgress != mTargetProgress) {
          shouldInvalidate = true
          val deltaTime = (SystemClock.uptimeMillis() - mLastTimeAnimated).toFloat() / 1000
          val deltaNormalized = deltaTime * mSpinSpeed
          mCurrentProgress = if (mCurrentProgress > mTargetProgress) {
            Math.max(mCurrentProgress - deltaNormalized, mTargetProgress)
          } else {
            Math.min(mCurrentProgress + deltaNormalized, mTargetProgress)
          }
          mLastTimeAnimated = SystemClock.uptimeMillis()
        }
        canvas.drawArc(mProgressCircleBounds, -90f, mCurrentProgress, false, mProgressPaint)
      }
      if (shouldInvalidate) {
        invalidate()
      }
    }
  }

  private fun updateProgressLength(deltaTimeInMillis: Long) {
    if (mPausedTimeWithoutGrowing >= PAUSE_GROWING_TIME) {
      mTimeStartGrowing += deltaTimeInMillis.toDouble()
      if (mTimeStartGrowing > BAR_SPIN_CYCLE_TIME) {
        mTimeStartGrowing -= BAR_SPIN_CYCLE_TIME
        mPausedTimeWithoutGrowing = 0
        mBarGrowingFromFront = !mBarGrowingFromFront
      }
      val distance = Math.cos((mTimeStartGrowing / BAR_SPIN_CYCLE_TIME + 1) * Math.PI)
        .toFloat() / 2 + 0.5f
      val length = (BAR_MAX_LENGTH - mBarLength).toFloat()
      if (mBarGrowingFromFront) {
        mBarExtraLength = distance * length
      } else {
        val newLength = length * (1 - distance)
        mCurrentProgress += mBarExtraLength - newLength
        mBarExtraLength = newLength
      }
    } else {
      mPausedTimeWithoutGrowing += deltaTimeInMillis
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    saveButtonOriginalPosition()
    if (mShouldProgressIndeterminate) {
      setIndeterminate(true)
      mShouldProgressIndeterminate = false
    } else if (mShouldSetProgress) {
      setProgress(mProgress, mAnimateProgress)
      mShouldSetProgress = false
    } else if (mShouldUpdateButtonPosition) {
      updateButtonPosition()
      mShouldUpdateButtonPosition = false
    }
    super.onSizeChanged(w, h, oldw, oldh)
    setupProgressBounds()
    setupProgressBarPaints()
    updateBackground()
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  override fun setLayoutParams(params: ViewGroup.LayoutParams) {
    if (params is MarginLayoutParams && mUsingElevationCompat) {
      params.leftMargin += shadowX
      params.topMargin += shadowY
      params.rightMargin += shadowX
      params.bottomMargin += shadowY
    }
    super.setLayoutParams(params)
  }

  fun updateBackground() {
    val layerDrawable: LayerDrawable
    layerDrawable = if (hasShadow()) {
      LayerDrawable(
        arrayOf(
          Shadow(),
          createFillDrawable(),
          iconDrawable
        )
      )
    } else {
      LayerDrawable(
        arrayOf(
          createFillDrawable(),
          iconDrawable
        )
      )
    }
    var iconSize = -1
    if (iconDrawable != null) {
      iconSize = Math.max(iconDrawable!!.intrinsicWidth, iconDrawable!!.intrinsicHeight)
    }
    val iconOffset = (circleSize - if (iconSize > 0) iconSize else mIconSize) / 2
    var circleInsetHorizontal = if (hasShadow()) mShadowRadius + Math.abs(mShadowXOffset) else 0
    var circleInsetVertical = if (hasShadow()) mShadowRadius + Math.abs(mShadowYOffset) else 0
    if (mProgressBarEnabled) {
      circleInsetHorizontal += mProgressWidth
      circleInsetVertical += mProgressWidth
    }

    /*layerDrawable.setLayerInset(
                mShowShadow ? 1 : 0,
                circleInsetHorizontal,
                circleInsetVertical,
                circleInsetHorizontal,
                circleInsetVertical
        );*/layerDrawable.setLayerInset(
      if (hasShadow()) 2 else 1,
      circleInsetHorizontal + iconOffset,
      circleInsetVertical + iconOffset,
      circleInsetHorizontal + iconOffset,
      circleInsetVertical + iconOffset
    )
    setBackgroundCompat(layerDrawable)
  }

  protected val iconDrawable: Drawable?
    protected get() = if (mIcon != null) {
      mIcon
    } else {
      ColorDrawable(Color.TRANSPARENT)
    }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private fun createFillDrawable(): Drawable {
    val drawable = StateListDrawable()
    drawable.addState(
      intArrayOf(-android.R.attr.state_enabled),
      createCircleDrawable(mColorDisabled)
    )
    drawable.addState(intArrayOf(android.R.attr.state_pressed), createCircleDrawable(mColorPressed))
    drawable.addState(intArrayOf(), createCircleDrawable(mColorNormal))
    if (Util.hasLollipop()) {
      val ripple = RippleDrawable(
        ColorStateList(arrayOf(intArrayOf()), intArrayOf(mColorRipple)),
        drawable,
        null
      )
      outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
          outline.setOval(0, 0, view.width, view.height)
        }
      }
      clipToOutline = true
      mBackgroundDrawable = ripple
      return ripple
    }
    mBackgroundDrawable = drawable
    return drawable
  }

  private fun createCircleDrawable(color: Int): Drawable {
    val shapeDrawable = CircleDrawable(OvalShape())
    shapeDrawable.paint.color = color
    return shapeDrawable
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private fun setBackgroundCompat(drawable: Drawable) {
    if (Util.hasJellyBean()) {
      background = drawable
    } else {
      setBackgroundDrawable(drawable)
    }
  }

  private fun saveButtonOriginalPosition() {
    if (!mButtonPositionSaved) {
      if (mOriginalX == -1f) {
        mOriginalX = x
      }
      if (mOriginalY == -1f) {
        mOriginalY = y
      }
      mButtonPositionSaved = true
    }
  }

  private fun updateButtonPosition() {
    val x: Float
    val y: Float
    if (mProgressBarEnabled) {
      x = if (mOriginalX > getX()) getX() + mProgressWidth else getX() - mProgressWidth
      y = if (mOriginalY > getY()) getY() + mProgressWidth else getY() - mProgressWidth
    } else {
      x = mOriginalX
      y = mOriginalY
    }
    setX(x)
    setY(y)
  }

  private fun setupProgressBarPaints() {
    mBackgroundPaint.color = mProgressBackgroundColor
    mBackgroundPaint.style = Paint.Style.STROKE
    mBackgroundPaint.strokeWidth = mProgressWidth.toFloat()
    mProgressPaint.color = mProgressColor
    mProgressPaint.style = Paint.Style.STROKE
    mProgressPaint.strokeWidth = mProgressWidth.toFloat()
  }

  private fun setupProgressBounds() {
    val circleInsetHorizontal = if (hasShadow()) shadowX else 0
    val circleInsetVertical = if (hasShadow()) shadowY else 0
    mProgressCircleBounds = RectF(
      (circleInsetHorizontal + mProgressWidth / 2).toFloat(),
      (circleInsetVertical + mProgressWidth / 2).toFloat(),
      (calculateMeasuredWidth() - circleInsetHorizontal - mProgressWidth / 2).toFloat(),
      (calculateMeasuredHeight() - circleInsetVertical - mProgressWidth / 2).toFloat()
    )
  }

  fun playShowAnimation() {
    hideAnimation!!.cancel()
    startAnimation(showAnimation)
  }

  fun playHideAnimation() {
    showAnimation!!.cancel()
    startAnimation(hideAnimation)
  }

  fun getOnClickListener(): OnClickListener? {
    return mClickListener
  }

  val labelView: Label?
    get() = getTag(R.id.fab_label) as? Label

  fun setColors(colorNormal: Int, colorPressed: Int, colorRipple: Int) {
    mColorNormal = colorNormal
    mColorPressed = colorPressed
    mColorRipple = colorRipple
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  fun onActionDown() {
    if (mBackgroundDrawable is StateListDrawable) {
      val drawable = mBackgroundDrawable as StateListDrawable
      drawable.state = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed)
    } else if (Util.hasLollipop()) {
      val ripple = mBackgroundDrawable as RippleDrawable?
      ripple!!.state = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed)
      ripple.setHotspot(calculateCenterX(), calculateCenterY())
      ripple.setVisible(true, true)
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  fun onActionUp() {
    if (mBackgroundDrawable is StateListDrawable) {
      val drawable = mBackgroundDrawable as StateListDrawable
      drawable.state = intArrayOf(android.R.attr.state_enabled)
    } else if (Util.hasLollipop()) {
      val ripple = mBackgroundDrawable as RippleDrawable?
      ripple!!.state = intArrayOf(android.R.attr.state_enabled)
      ripple.setHotspot(calculateCenterX(), calculateCenterY())
      ripple.setVisible(true, true)
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (mClickListener != null && isEnabled) {
      val label = getTag(R.id.fab_label) as Label
        ?: return super.onTouchEvent(event)
      val action = event.action
      when (action) {
        MotionEvent.ACTION_UP -> {
          if (label != null) {
            label.onActionUp()
          }
          onActionUp()
        }
        MotionEvent.ACTION_CANCEL -> {
          if (label != null) {
            label.onActionUp()
          }
          onActionUp()
        }
      }
      mGestureDetector.onTouchEvent(event)
    }
    return super.onTouchEvent(event)
  }

  var mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent): Boolean {
      val label = getTag(R.id.fab_label) as Label
      label?.onActionDown()
      onActionDown()
      return super.onDown(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
      val label = getTag(R.id.fab_label) as Label
      label?.onActionUp()
      onActionUp()
      return super.onSingleTapUp(e)
    }
  })

  public override fun onSaveInstanceState(): Parcelable? {
    val superState = super.onSaveInstanceState()
    val ss = ProgressSavedState(superState)
    ss.mCurrentProgress = mCurrentProgress
    ss.mTargetProgress = mTargetProgress
    ss.mSpinSpeed = mSpinSpeed
    ss.mProgressWidth = mProgressWidth
    ss.mProgressColor = mProgressColor
    ss.mProgressBackgroundColor = mProgressBackgroundColor
    ss.mShouldProgressIndeterminate = mProgressIndeterminate
    ss.mShouldSetProgress = mProgressBarEnabled && mProgress > 0 && !mProgressIndeterminate
    ss.mProgress = mProgress
    ss.mAnimateProgress = mAnimateProgress
    ss.mShowProgressBackground = isProgressBackgroundShown
    return ss
  }

  public override fun onRestoreInstanceState(state: Parcelable) {
    if (state !is ProgressSavedState) {
      super.onRestoreInstanceState(state)
      return
    }
    val ss = state
    super.onRestoreInstanceState(ss.superState)
    mCurrentProgress = ss.mCurrentProgress
    mTargetProgress = ss.mTargetProgress
    mSpinSpeed = ss.mSpinSpeed
    mProgressWidth = ss.mProgressWidth
    mProgressColor = ss.mProgressColor
    mProgressBackgroundColor = ss.mProgressBackgroundColor
    mShouldProgressIndeterminate = ss.mShouldProgressIndeterminate
    mShouldSetProgress = ss.mShouldSetProgress
    mProgress = ss.mProgress
    mAnimateProgress = ss.mAnimateProgress
    isProgressBackgroundShown = ss.mShowProgressBackground
    mLastTimeAnimated = SystemClock.uptimeMillis()
  }

  private inner class CircleDrawable : ShapeDrawable {
    private var circleInsetHorizontal = 0
    private var circleInsetVertical = 0

    private constructor() {}
    constructor(s: Shape) : super(s) {
      circleInsetHorizontal = if (hasShadow()) mShadowRadius + Math.abs(mShadowXOffset) else 0
      circleInsetVertical = if (hasShadow()) mShadowRadius + Math.abs(mShadowYOffset) else 0
      if (mProgressBarEnabled) {
        circleInsetHorizontal += mProgressWidth
        circleInsetVertical += mProgressWidth
      }
    }

    override fun draw(canvas: Canvas) {
      setBounds(
        circleInsetHorizontal, circleInsetVertical, calculateMeasuredWidth()
                - circleInsetHorizontal, calculateMeasuredHeight() - circleInsetVertical
      )
      super.draw(canvas)
    }
  }

  private inner class Shadow() : Drawable() {
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mErase = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRadius = 0f
    private fun init() {
      setLayerType(LAYER_TYPE_SOFTWARE, null)
      mPaint.style = Paint.Style.FILL
      mPaint.color = mColorNormal
      mErase.xfermode = PORTER_DUFF_CLEAR
      if (!isInEditMode) {
        mPaint.setShadowLayer(
          mShadowRadius.toFloat(),
          mShadowXOffset.toFloat(),
          mShadowYOffset.toFloat(),
          mShadowColor
        )
      }
      mRadius = (circleSize / 2).toFloat()
      if (mProgressBarEnabled && isProgressBackgroundShown) {
        mRadius += mProgressWidth.toFloat()
      }
    }

    override fun draw(canvas: Canvas) {
      canvas.drawCircle(calculateCenterX(), calculateCenterY(), mRadius, mPaint)
      canvas.drawCircle(calculateCenterX(), calculateCenterY(), mRadius, mErase)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}
    override fun getOpacity(): Int {
      return PixelFormat.OPAQUE
    }

    init {
      this.init()
    }
  }

  internal class ProgressSavedState : BaseSavedState {
    var mCurrentProgress = 0f
    var mTargetProgress = 0f
    var mSpinSpeed = 0f
    var mProgress = 0
    var mProgressWidth = 0
    var mProgressColor = 0
    var mProgressBackgroundColor = 0
    var mProgressBarEnabled = false
    var mProgressBarVisibilityChanged = false
    var mProgressIndeterminate = false
    var mShouldProgressIndeterminate = false
    var mShouldSetProgress = false
    var mAnimateProgress = false
    var mShowProgressBackground = false

    constructor(superState: Parcelable?) : super(superState) {}
    private constructor(inn: Parcel) : super(inn) {
      mCurrentProgress = inn.readFloat()
      mTargetProgress = inn.readFloat()
      mProgressBarEnabled = inn.readInt() != 0
      mSpinSpeed = inn.readFloat()
      mProgress = inn.readInt()
      mProgressWidth = inn.readInt()
      mProgressColor = inn.readInt()
      mProgressBackgroundColor = inn.readInt()
      mProgressBarVisibilityChanged = inn.readInt() != 0
      mProgressIndeterminate = inn.readInt() != 0
      mShouldProgressIndeterminate = inn.readInt() != 0
      mShouldSetProgress = inn.readInt() != 0
      mAnimateProgress = inn.readInt() != 0
      mShowProgressBackground = inn.readInt() != 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeFloat(mCurrentProgress)
      out.writeFloat(mTargetProgress)
      out.writeInt(if (mProgressBarEnabled) 1 else 0)
      out.writeFloat(mSpinSpeed)
      out.writeInt(mProgress)
      out.writeInt(mProgressWidth)
      out.writeInt(mProgressColor)
      out.writeInt(mProgressBackgroundColor)
      out.writeInt(if (mProgressBarVisibilityChanged) 1 else 0)
      out.writeInt(if (mProgressIndeterminate) 1 else 0)
      out.writeInt(if (mShouldProgressIndeterminate) 1 else 0)
      out.writeInt(if (mShouldSetProgress) 1 else 0)
      out.writeInt(if (mAnimateProgress) 1 else 0)
      out.writeInt(if (mShowProgressBackground) 1 else 0)
    }

    companion object {
      @JvmField val CREATOR: Parcelable.Creator<ProgressSavedState?> =
        object : Parcelable.Creator<ProgressSavedState?> {
          override fun createFromParcel(`in`: Parcel): ProgressSavedState? {
            return ProgressSavedState(`in`)
          }

          override fun newArray(size: Int): Array<ProgressSavedState?> {
            return arrayOfNulls(size)
          }
        }
    }
  }

  /* ===== API methods ===== */
  override fun setImageDrawable(drawable: Drawable?) {
    if (mIcon !== drawable) {
      mIcon = drawable
      updateBackground()
    }
  }

  override fun setImageResource(resId: Int) {
    val drawable = resources.getDrawable(resId)
    if (mIcon !== drawable) {
      mIcon = drawable
      updateBackground()
    }
  }

  override fun setOnClickListener(l: OnClickListener?) {
    super.setOnClickListener(l)
    mClickListener = l
    val label = getTag(R.id.fab_label) as View
    label?.setOnClickListener {
      if (mClickListener != null) {
        mClickListener!!.onClick(this@FloatingActionButton)
      }
    }
  }

  /**
   * Sets the size of the **FloatingActionButton** and invalidates its layout.
   *
   * @param size size of the **FloatingActionButton**. Accepted values: SIZE_NORMAL, SIZE_MINI.
   */
  var buttonSize: Int
    get() = mFabSize
    set(size) {
      require(!(size != SIZE_NORMAL && size != SIZE_MINI)) { "Use @FabSize constants only!" }
      if (mFabSize != size) {
        mFabSize = size
        updateBackground()
      }
    }

  fun setColorNormalResId(colorResId: Int) {
    colorNormal = resources.getColor(colorResId)
  }

  var colorNormal: Int
    get() = mColorNormal
    set(color) {
      if (mColorNormal != color) {
        mColorNormal = color
        updateBackground()
      }
    }

  fun setColorPressedResId(colorResId: Int) {
    colorPressed = resources.getColor(colorResId)
  }

  var colorPressed: Int
    get() = mColorPressed
    set(color) {
      if (color != mColorPressed) {
        mColorPressed = color
        updateBackground()
      }
    }

  fun setColorRippleResId(colorResId: Int) {
    colorRipple = resources.getColor(colorResId)
  }

  var colorRipple: Int
    get() = mColorRipple
    set(color) {
      if (color != mColorRipple) {
        mColorRipple = color
        updateBackground()
      }
    }

  fun setColorDisabledResId(colorResId: Int) {
    colorDisabled = resources.getColor(colorResId)
  }

  var colorDisabled: Int
    get() = mColorDisabled
    set(color) {
      if (color != mColorDisabled) {
        mColorDisabled = color
        updateBackground()
      }
    }

  fun setShowShadow(show: Boolean) {
    if (mShowShadow != show) {
      mShowShadow = show
      updateBackground()
    }
  }

  fun hasShadow(): Boolean {
    return !mUsingElevation && mShowShadow
  }

  /**
   * Sets the shadow radius of the **FloatingActionButton** and invalidates its layout.
   *
   *
   * Must be specified in density-independent (dp) pixels, which are then converted into actual
   * pixels (px).
   *
   * @param shadowRadiusDp shadow radius specified in density-independent (dp) pixels
   */
  fun setShadowRadius(shadowRadiusDp: Float) {
    mShadowRadius = Util.dpToPx(context, shadowRadiusDp)
    requestLayout()
    updateBackground()
  }

  /**
   * Sets the shadow radius of the **FloatingActionButton** and invalidates its layout.
   *
   * @param dimenResId the resource identifier of the dimension
   */
  var shadowRadius: Int
    get() = mShadowRadius
    set(dimenResId) {
      val shadowRadius = resources.getDimensionPixelSize(dimenResId)
      if (mShadowRadius != shadowRadius) {
        mShadowRadius = shadowRadius
        requestLayout()
        updateBackground()
      }
    }

  /**
   * Sets the shadow x offset of the **FloatingActionButton** and invalidates its layout.
   *
   *
   * Must be specified in density-independent (dp) pixels, which are then converted into actual
   * pixels (px).
   *
   * @param shadowXOffsetDp shadow radius specified in density-independent (dp) pixels
   */
  fun setShadowXOffset(shadowXOffsetDp: Float) {
    mShadowXOffset = Util.dpToPx(context, shadowXOffsetDp)
    requestLayout()
    updateBackground()
  }

  /**
   * Sets the shadow x offset of the **FloatingActionButton** and invalidates its layout.
   *
   * @param dimenResId the resource identifier of the dimension
   */
  var shadowXOffset: Int
    get() = mShadowXOffset
    set(dimenResId) {
      val shadowXOffset = resources.getDimensionPixelSize(dimenResId)
      if (mShadowXOffset != shadowXOffset) {
        mShadowXOffset = shadowXOffset
        requestLayout()
        updateBackground()
      }
    }

  /**
   * Sets the shadow y offset of the **FloatingActionButton** and invalidates its layout.
   *
   *
   * Must be specified in density-independent (dp) pixels, which are then converted into actual
   * pixels (px).
   *
   * @param shadowYOffsetDp shadow radius specified in density-independent (dp) pixels
   */
  fun setShadowYOffset(shadowYOffsetDp: Float) {
    mShadowYOffset = Util.dpToPx(context, shadowYOffsetDp)
    requestLayout()
    updateBackground()
  }

  /**
   * Sets the shadow y offset of the **FloatingActionButton** and invalidates its layout.
   *
   * @param dimenResId the resource identifier of the dimension
   */
  var shadowYOffset: Int
    get() = mShadowYOffset
    set(dimenResId) {
      val shadowYOffset = resources.getDimensionPixelSize(dimenResId)
      if (mShadowYOffset != shadowYOffset) {
        mShadowYOffset = shadowYOffset
        requestLayout()
        updateBackground()
      }
    }

  fun setShadowColorResource(colorResId: Int) {
    val shadowColor = resources.getColor(colorResId)
    if (mShadowColor != shadowColor) {
      mShadowColor = shadowColor
      updateBackground()
    }
  }

  var shadowColor: Int
    get() = mShadowColor
    set(color) {
      if (mShadowColor != color) {
        mShadowColor = color
        updateBackground()
      }
    }

  /**
   * Checks whether **FloatingActionButton** is hidden
   *
   * @return true if **FloatingActionButton** is hidden, false otherwise
   */
  val isHidden: Boolean
    get() = visibility == INVISIBLE

  /**
   * Makes the **FloatingActionButton** to appear and sets its visibility to [.VISIBLE]
   *
   * @param animate if true - plays "show animation"
   */
  fun show(animate: Boolean) {
    if (isHidden) {
      if (animate) {
        playShowAnimation()
      }
      super.setVisibility(VISIBLE)
    }
  }

  /**
   * Makes the **FloatingActionButton** to disappear and sets its visibility to [.INVISIBLE]
   *
   * @param animate if true - plays "hide animation"
   */
  fun hide(animate: Boolean) {
    if (!isHidden) {
      if (animate) {
        playHideAnimation()
      }
      super.setVisibility(INVISIBLE)
    }
  }

  fun toggle(animate: Boolean) {
    if (isHidden) {
      show(animate)
    } else {
      hide(animate)
    }
  }

  var labelText: String?
    get() = mLabelText
    set(text) {
      mLabelText = text
      val labelView: TextView? = labelView
      if (labelView != null) {
        labelView.text = text
      }
    }
  var labelVisibility: Int
    get() {
      val labelView: TextView? = labelView
      return labelView?.visibility ?: -1
    }
    set(visibility) {
      val labelView: Label? = labelView
      if (labelView != null) {
        labelView.visibility = visibility
        labelView.isHandleVisibilityChanges = (visibility == VISIBLE)
      }
    }

  override fun setElevation(elevation: Float) {
    if (Util.hasLollipop() && elevation > 0) {
      super.setElevation(elevation)
      if (!isInEditMode) {
        mUsingElevation = true
        mShowShadow = false
      }
      updateBackground()
    }
  }

  /**
   * Sets the shadow color and radius to mimic the native elevation.
   *
   *
   * **API 21+**: Sets the native elevation of this view, in pixels. Updates margins to
   * make the view hold its position in layout across different platform versions.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  fun setElevationCompat(elevation: Float) {
    mShadowColor = 0x26000000
    mShadowRadius = Math.round(elevation / 2)
    mShadowXOffset = 0
    mShadowYOffset = Math.round(if (mFabSize == SIZE_NORMAL) elevation else elevation / 2)
    if (Util.hasLollipop()) {
      super.setElevation(elevation)
      mUsingElevationCompat = true
      mShowShadow = false
      updateBackground()
      val layoutParams = layoutParams
      layoutParams?.let { setLayoutParams(it) }
    } else {
      mShowShadow = true
      updateBackground()
    }
  }

  /**
   *
   * Change the indeterminate mode for the progress bar. In indeterminate
   * mode, the progress is ignored and the progress bar shows an infinite
   * animation instead.
   *
   * @param indeterminate true to enable the indeterminate mode
   */
  @Synchronized
  fun setIndeterminate(indeterminate: Boolean) {
    if (!indeterminate) {
      mCurrentProgress = 0.0f
    }
    mProgressBarEnabled = indeterminate
    mShouldUpdateButtonPosition = true
    mProgressIndeterminate = indeterminate
    mLastTimeAnimated = SystemClock.uptimeMillis()
    setupProgressBounds()
    //        saveButtonOriginalPosition();
    updateBackground()
  }

  @Synchronized
  fun setProgress(progress: Int, animate: Boolean) {
    var progress = progress
    if (mProgressIndeterminate) return
    mProgress = progress
    mAnimateProgress = animate
    if (!mButtonPositionSaved) {
      mShouldSetProgress = true
      return
    }
    mProgressBarEnabled = true
    mShouldUpdateButtonPosition = true
    setupProgressBounds()
    saveButtonOriginalPosition()
    updateBackground()
    if (progress < 0) {
      progress = 0
    } else if (progress > max) {
      progress = max
    }
    if (progress.toFloat() == mTargetProgress) {
      return
    }
    mTargetProgress = if (max > 0) progress / max.toFloat() * 360 else 0F
    mLastTimeAnimated = SystemClock.uptimeMillis()
    if (!animate) {
      mCurrentProgress = mTargetProgress
    }
    invalidate()
  }

  @get:Synchronized
  val progress: Int
    get() = if (mProgressIndeterminate) 0 else mProgress

  @Synchronized
  fun hideProgress() {
    mProgressBarEnabled = false
    mShouldUpdateButtonPosition = true
    updateBackground()
  }

  @Synchronized
  fun setShowProgressBackground(show: Boolean) {
    isProgressBackgroundShown = show
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    val label = getTag(R.id.fab_label) as Label
    if (label != null) {
      label.isEnabled = enabled
    }
  }

  override fun setVisibility(visibility: Int) {
    super.setVisibility(visibility)
    val label = getTag(R.id.fab_label) as Label
    if (label != null) {
      label.visibility = visibility
    }
  }

  /**
   * **This will clear all AnimationListeners.**
   */
  fun hideButtonInMenu(animate: Boolean) {
    if (!isHidden && visibility != GONE) {
      hide(animate)
      val label = labelView
      if (label != null) {
        label.hide(animate)
      }
      hideAnimation!!.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}
        override fun onAnimationEnd(animation: Animation) {
          visibility = GONE
          hideAnimation!!.setAnimationListener(null)
        }

        override fun onAnimationRepeat(animation: Animation) {}
      })
    }
  }

  fun showButtonInMenu(animate: Boolean) {
    if (visibility == VISIBLE) return
    visibility = INVISIBLE
    show(animate)
    val label = labelView
    if (label != null) {
      label.show(animate)
    }
  }

  /**
   * Set the label's background colors
   */
  fun setLabelColors(colorNormal: Int, colorPressed: Int, colorRipple: Int) {
    val label = labelView
    val left = label?.paddingLeft
    val top = label?.paddingTop
    val right = label?.paddingRight
    val bottom = label?.paddingBottom
    label?.setColors(colorNormal, colorPressed, colorRipple)
    label?.updateBackground()
    label?.setPadding(left!!, top!!, right!!, bottom!!)
  }

  fun setLabelTextColor(color: Int) {
    labelView?.setTextColor(color)
  }

  fun setLabelTextColor(colors: ColorStateList?) {
    labelView?.setTextColor(colors)
  }

  companion object {
    const val SIZE_NORMAL = 0
    const val SIZE_MINI = 1
    private val PORTER_DUFF_CLEAR: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private const val PAUSE_GROWING_TIME: Long = 200
    private const val BAR_SPIN_CYCLE_TIME = 500.0
    private const val BAR_MAX_LENGTH = 270
  }
}
