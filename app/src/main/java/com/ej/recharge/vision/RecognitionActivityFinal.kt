/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ej.recharge.vision

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.ej.recharge.R
import com.ej.recharge.utils.Util
import com.ej.recharge.vision.barcodescanner.BarcodeScannerProcessor
import com.ej.recharge.vision.cameraview.Option
import com.ej.recharge.vision.cameraview.OptionView
import com.ej.recharge.vision.facedetector.FaceDetectorProcessor
import com.ej.recharge.vision.labeldetector.LabelDetectorProcessor
import com.ej.recharge.vision.objectdetector.ObjectDetectorProcessor
import com.ej.recharge.vision.posedetector.PoseDetectorProcessor
import com.ej.recharge.vision.preference.PreferenceUtils
import com.ej.recharge.vision.preference.SettingsActivity
import com.ej.recharge.vision.preference.SettingsActivity.LaunchSource
import com.ej.recharge.vision.segmenter.SegmenterProcessor
import com.ej.recharge.vision.textdetector.TextGraphicSelected
import com.ej.recharge.vision.textdetector.TextGraphicUnselected
import com.ej.recharge.vision.textdetector.TextRecognitionProcessor
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
//import com.google.mlkit.vision.text.TextRecognizerOptions
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import kotlinx.android.synthetic.main.vision_activity_still_image_final.*
import java.io.*
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.max

@KeepName
class RecognitionActivityFinal : AppCompatActivity(),
  ActivityCompat.OnRequestPermissionsResultCallback,
  CompoundButton.OnCheckedChangeListener,
  OptionView.Callback,
  GestureDetector.OnGestureListener,
  GestureDetector.OnDoubleTapListener
{

  private var preview: ImageView? = null
  private var graphicOverlay: GraphicOverlay? = null
  private var selectedMode = OBJECT_DETECTION
  private var selectedSize: String? = SIZE_SCREEN
  private var isLandScape = false
  private var imageUri: Uri? = null
  private var imageMaxWidth = 0
  private var imageMaxHeight = 0
  private var imageProcessor: VisionImageProcessor? = null


//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

  private var cameraSource: CameraSource? = null
  private var previewView: CameraSourcePreview? = null
  private var didTakePicture: Boolean = false

  private var controlLayout: FrameLayout? = null

  override fun <T : Any> onValueChanged(option: Option<T>, value: T, name: String): Boolean {
    if (option is Option.Width || option is Option.Height) {
      val preview = camera.preview
      val wrapContent = value as Int == ViewGroup.LayoutParams.WRAP_CONTENT
      if (preview == Preview.SURFACE && !wrapContent) {
        message(
          "The SurfaceView preview does not support width or height changes. " +
                  "The view will act as WRAP_CONTENT by default.", true
        )
        return false
      }
    }
    option.set(camera, value)
    message("Changed " + option.name + " to " + name, false)
    return true
  }

  private fun message(content: String, important: Boolean) {
    if (important) {
      LOG.e(content)
      Toast.makeText(this, content, Toast.LENGTH_LONG).show()
    } else {
      LOG.w(content)
      Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {

    Log.i(TAG, "Permission granted!")
    createCameraSource(selectedMode)

    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

  }

  private val requiredPermissions: Array<String?>

    get() =

      try {

      val info = this.packageManager
        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)

      val ps = info.requestedPermissions

      if (ps != null && ps.isNotEmpty()) {
        ps
      }
      else {
        arrayOfNulls(0)
      }

    }
      catch (e: Exception) {

      arrayOfNulls(0)

    }

  private fun allPermissionsGranted(): Boolean {

    for (permission in requiredPermissions) {

      if (!isPermissionGranted(this, permission)) {
        return false
      }

    }
    return true
  }

  private val runtimePermissions: Unit

    get() {

      val allNeededPermissions: MutableList<String?> = ArrayList()

      for (permission in requiredPermissions) {
        if (!isPermissionGranted(this, permission)) {
          allNeededPermissions.add(permission)
        }
      }

      if (allNeededPermissions.isNotEmpty()) {

        ActivityCompat.requestPermissions(
          this,
          allNeededPermissions.toTypedArray(),
          PERMISSION_REQUESTS
        )

      }

    }

  private fun createCameraSource(model: String) {

    if (cameraSource == null) {
      cameraSource = CameraSource(this, graphicOverlay)
    }

    try {

      when (model) {

        TEXT_RECOGNITION -> {

          Log.i(TAG, "Using on-device Text recognition Processor")

          cameraSource!!
            .setMachineLearningFrameProcessor(
              TextRecognitionProcessor(this,
                TextRecognizerOptions.Builder().build()
              )
            )

        }
        OBJECT_DETECTION -> {

          Log.i(TAG, "Using Object Detector Processor")

          val objectDetectorOptions = PreferenceUtils.getObjectDetectorOptionsForLivePreview(this)

          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(
              this,
              objectDetectorOptions
            )
          )

        }
        OBJECT_DETECTION_CUSTOM -> {

          Log.i(TAG, "Using Custom Object Detector Processor")

          val localModel = LocalModel.Builder()
            .setAssetFilePath("custom_models/bird_classifier.tflite")
            .build()

          val customObjectDetectorOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel)

          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(
              this,
              customObjectDetectorOptions
            )
          )

        }
        CUSTOM_AUTOML_OBJECT_DETECTION -> {

          Log.i(TAG, "Using Custom AutoML Object Detector Processor")

          val customAutoMLODTLocalModel = LocalModel.Builder()
            .setAssetManifestFilePath("automl/manifest.json")
            .build()

          val customAutoMLODTOptions = PreferenceUtils
            .getCustomObjectDetectorOptionsForLivePreview(this, customAutoMLODTLocalModel)

          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(
              this,
              customAutoMLODTOptions
            )
          )

        }
        FACE_DETECTION -> {

          Log.i(TAG, "Using Face Detector Processor")

          val faceDetectorOptions =
            PreferenceUtils.getFaceDetectorOptions(this)

          cameraSource!!.setMachineLearningFrameProcessor(
            FaceDetectorProcessor(
              this,
              faceDetectorOptions
            )
          )

        }
        BARCODE_SCANNING -> {

          Log.i(TAG, "Using Barcode Detector Processor")

          cameraSource!!.setMachineLearningFrameProcessor(
            BarcodeScannerProcessor(this)
          )

        }
        IMAGE_LABELING -> {

          Log.i(TAG, "Using Image Label Detector Processor")

          cameraSource!!.setMachineLearningFrameProcessor(
            LabelDetectorProcessor(
              this,
              ImageLabelerOptions.DEFAULT_OPTIONS
            )
          )

        }
        IMAGE_LABELING_CUSTOM -> {

          Log.i(TAG, "Using Custom Image Label Detector Processor")

          val localClassifier = LocalModel.Builder()
            .setAssetFilePath("custom_models/bird_classifier.tflite")
            .build()

          val customImageLabelerOptions =
            CustomImageLabelerOptions.Builder(localClassifier).build()
          cameraSource!!.setMachineLearningFrameProcessor(
            LabelDetectorProcessor(
              this,
              customImageLabelerOptions
            )
          )

        }
        CUSTOM_AUTOML_LABELING -> {

          Log.i(TAG, "Using Custom AutoML Image Label Detector Processor")

          val customAutoMLLabelLocalModel = LocalModel.Builder()
            .setAssetManifestFilePath("automl/manifest.json")
            .build()

          val customAutoMLLabelOptions = CustomImageLabelerOptions
            .Builder(customAutoMLLabelLocalModel)
            .setConfidenceThreshold(0f)
            .build()

          cameraSource!!.setMachineLearningFrameProcessor(
            LabelDetectorProcessor(
              this,
              customAutoMLLabelOptions
            )
          )

        }
        POSE_DETECTION -> {

          val poseDetectorOptions = PreferenceUtils.getPoseDetectorOptionsForLivePreview(this)

          Log.i(TAG, "Using Pose Detector with options $poseDetectorOptions")

          val shouldShowInFrameLikelihood =
            PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this)

          val visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)

          val rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)

          val runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this)

          cameraSource!!.setMachineLearningFrameProcessor(
            PoseDetectorProcessor(
              this, poseDetectorOptions, shouldShowInFrameLikelihood, visualizeZ, rescaleZ,
              runClassification, /* isStreamMode = */ true
            )

          )

        }
        SELFIE_SEGMENTATION -> {

          cameraSource!!.setMachineLearningFrameProcessor(SegmenterProcessor(this))

        }
        else -> Log.e(TAG, "Unknown model: $model")

      }

    }
    catch (e: Exception) {

      Log.e(TAG, "Can not create image processor: $model", e)

      Toast.makeText(
        applicationContext, "Can not create image processor: " + e.message,
        Toast.LENGTH_LONG
      )
        .show()

    }

  }

  override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {

    Log.e(TAG, "Set facing")

    if (cameraSource != null) {

      if (isChecked) {
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
      }
      else {
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
      }

      toggle = isChecked

    }


    previewView?.stop()
    startCameraSource()

  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private fun startCameraSource() {
    if (cameraSource != null) {
      try {
        if (previewView == null) {
          Log.e(TAG, "resume: Preview is null")
        }
        if (graphicOverlay == null) {
          Log.e(TAG, "resume: graphOverlay is null")
        }
        previewView!!.start(cameraSource, graphicOverlay)
      } catch (e: IOException) {
        Log.e(TAG, "Unable to start camera source.", e)
        cameraSource!!.release()
        cameraSource = null
      }
    }
  }


  var toggle: Boolean = false

  private fun colorCountDown() {

    Handler().postDelayed({

      object : CountDownTimer(3000, 25) {

        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {

        }

      }.start()

    }, 0)

  }

  @Suppress("DEPRECATION")
  private fun takePicture() {

    didTakePicture = true

//    previewView?.cameraSource?.camera?.release()
    camera.open()
    camera.visibility = View.VISIBLE

    preview?.visibility = View.INVISIBLE
    previewView?.visibility = View.INVISIBLE
    graphicOverlay?.visibility = View.INVISIBLE
    controlLayout?.visibility = View.INVISIBLE

    if (toggle) {
      toggleCamera()
    }

//    if (camera.isTakingPicture) return
    captureTime = System.currentTimeMillis()

    Handler().postDelayed({

      object : CountDownTimer(500, 25) {

        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
          camera.takePicture()
        }

      }.start()

    }, 1000)

  }

  val util: Util = Util()

  private val camera: CameraView by lazy { findViewById(R.id.camera) }

  private val controlPanel: ViewGroup by lazy { findViewById(R.id.controls) }

  private var captureTime: Long = 0

  private var currentFilter = 0

  private val allFilters = Filters.values()

  private lateinit var mDetector: GestureDetectorCompat



  var chosenRechargeCode: String = ""

  private fun process() {

    if (codeList.size == 0) {

      val message: String =

        if (selectedMode != TEXT_RECOGNITION) {

          "Please choose Text Recognition and try again. " +
                  "Every other recognition is just for play."

      }
      else {

          "Please select a text with your finger inorder to proceed."

      }

      util.onShowMessage(message, context)
      return

    }

    if (selectedMode != TEXT_RECOGNITION) {

      val message: String = "Please choose Text Recognition and try again. " +
              "Every other recognition is just for play."
      util.onShowErrorMessage(message, context)

    }
    else {

      val rechargeCodeChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
        context,
        R.style.MyDialogTheme
      )
      rechargeCodeChoiceBuilder.setTitle("Choose your recharge code to edit")

      val intent = intent
      pAccount   = intent.getStringExtra("pAccount")!!
      phone      = intent.getStringExtra("phone")!!
      network    = intent.getStringExtra("network")!!

      rechargeCodeChoiceBuilder.setSingleChoiceItems(codeList.toTypedArray(), -1) {

          dialogInterface, i ->

        dialogInterface.dismiss()

        chosenRechargeCode = codeList[i]

        val reviewActivityIntent: Intent = Intent(
          context,
          RecognitionActivityReviewFinal::class.java
        )

        reviewActivityIntent.putExtra("code", chosenRechargeCode)
        reviewActivityIntent.putExtra("pAccount", pAccount)
        reviewActivityIntent.putExtra("phone", phone)
        reviewActivityIntent.putExtra("network", network)

        startActivity(reviewActivityIntent)
        finish()

      }

      rechargeCodeChoiceBuilder.setNegativeButton(android.R.string.cancel)
      { dialog, which ->
        dialog.cancel()
      }

      rechargeCodeChoiceBuilder.show()

    }




  }



  private lateinit var pAccount: String
  private lateinit var phone: String
  private lateinit var network: String
  private lateinit var code: String
  private lateinit var codeFin: String

  private var codeList: MutableList<String> = mutableListOf()

  private fun bestCodeCaptured(line: Text.Line) {

    val message: String = "\n\n\nFinal Detected Text: " + line.text + "\n\n\n"
    val message2: String = "Final Detected Text: " + line.text
    Log.e("ATTENTION ATTENTION", message)
//    util.onShowMessage(message2, context)

    codeList.add(line.text)

  }



  private fun onTap(rawX: Float, rawY: Float): Boolean {

    // Find tap point in preview frame coordinates.
    val location = IntArray(2)
    graphicOverlay?.getLocationOnScreen(location)

    val x: Float = (rawX - location[0]) / graphicOverlay!!.scaleFactor
    val y: Float = (rawY - location[1]) / graphicOverlay!!.scaleFactor

    // Find the barcode whose center is closest to the tapped point.
    var best: Text.Line? = null
    var bestDistance = Float.MAX_VALUE

    for (graphic in graphicOverlay!!.graphics) {

      if (graphic is TextGraphicUnselected) {

        for (textBlock in graphic.clientText.textBlocks) {

          for (line in textBlock.lines) {

            if (line.boundingBox!!.contains(x.toInt(), y.toInt())) {

              //Exact hit, no need to keep looking.
              best = line
              break

            }

            val dx: Float = x - line.boundingBox!!.centerX()
            val dy: Float = y - line.boundingBox!!.centerY()

            val distance = dx * dx + dy * dy // actually squared distance

            if (distance < bestDistance) {
              best = line
              bestDistance = distance
            }

          }


        }


      }

    }

    if (best != null) {

      val textGraphicSelected: TextGraphicSelected = TextGraphicSelected(graphicOverlay, best)

      graphicOverlay!!.add(textGraphicSelected)

      bestCodeCaptured(best)

      return true

    }

    return false

  }

  override fun onTouchEvent(event: MotionEvent): Boolean {

    return if (mDetector.onTouchEvent(event)) {
      true
    }
    else {
      super.onTouchEvent(event)
    }

  }

  override fun onDown(event: MotionEvent): Boolean {
    Log.e(TAG, "onDown: ${event.toString()}")
    return true
  }

  override fun onFling(
    event1: MotionEvent,
    event2: MotionEvent,
    velocityX: Float,
    velocityY: Float
  ): Boolean {
    Log.e(TAG, "onFling: ${event1.toString()} ${event2.toString()}")
    return true
  }

  override fun onLongPress(event: MotionEvent) {
    Log.e(TAG, "onLongPress: ${event.toString()}")
  }

  override fun onScroll(
    event1: MotionEvent,
    event2: MotionEvent,
    distanceX: Float,
    distanceY: Float
  ): Boolean {
    Log.e(TAG, "onScroll: ${event1.toString()} ${event2.toString()}")
    return true
  }

  override fun onShowPress(event: MotionEvent) {
    Log.e(TAG, "onShowPress: ${event.toString()}")
  }

  override fun onSingleTapUp(event: MotionEvent): Boolean {

    Log.e(TAG, "onSingleTapUp: ${event.toString()}")

    return true

  }

  override fun onDoubleTap(event: MotionEvent): Boolean {

    val message: String = "A Double Tap Occurred!: ${event.toString()}"

    Log.e("ATTENTION ATTENTION", message)

    return onTap(event.rawX, event.rawY)
  }

  override fun onDoubleTapEvent(event: MotionEvent): Boolean {
    Log.e(TAG, "onDoubleTapEvent: ${event.toString()}")
    return true
  }

  override fun onSingleTapConfirmed(event: MotionEvent): Boolean {

    val message: String = "A Single Tap Confirmed Occurred!: ${event.toString()}"
    //util.onShowMessage(message, context)
    Log.e("ATTENTION ATTENTION", message)
    Log.e(TAG, "onSingleTapConfirmed: ${event.toString()}")

    return onTap(event.rawX, event.rawY)

  }

//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

  private lateinit var context: Context

  private lateinit var selectImageButton: Button
  private lateinit var processButton: Button

  private lateinit var takeAnother: ImageButton
  private lateinit var snapCamera: ImageButton

  private fun onCreate() {



  }

  override fun onDestroy() {

    cameraSource = null
    preview = null
    graphicOverlay = null
    previewView = null

    super.onDestroy()
  }

  override fun onBackPressed() {

    camera.close()
    cameraSource?.clearProcessor()

    preview?.clearAnimation()
    preview?.clearFocus()
    graphicOverlay?.clear()
    graphicOverlay?.clearAnimation()
    graphicOverlay?.clearFocus()
    cameraSource = null
    preview = null
    graphicOverlay = null
    previewView = null

    super.onBackPressed()

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.vision_activity_still_image_final)

    onCreate()

    context = this

    util.addFontToAppBarTitle(supportActionBar!!, applicationContext)

    selectImageButton = findViewById<Button>(R.id.select_image_button)
    processButton = findViewById<Button>(R.id.process_button)

    selectImageButton
      .setOnClickListener {

          view: View ->

        val popup = PopupMenu(this@RecognitionActivityFinal, view)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->

          val itemId = menuItem.itemId

          if (itemId == R.id.select_images_from_local) {
            startChooseImageIntentForResult()
            return@setOnMenuItemClickListener true
          }
          else if (itemId == R.id.take_photo_using_camera) {
            startCameraIntentForResult()
            return@setOnMenuItemClickListener true
          }

          false

        }

        val inflater = popup.menuInflater

        inflater.inflate(R.menu.camera_button_menu, popup.menu)
        popup.show()

      }


    preview = findViewById(R.id.preview)
    graphicOverlay = findViewById(R.id.graphic_overlay)

    controlLayout = findViewById(R.id.control)

//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

    previewView = findViewById(R.id.preview_view)
    previewView?.isDrawingCacheEnabled = true

    val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
    facingSwitch.setOnCheckedChangeListener(this)

    processButton = findViewById<Button>(R.id.process_button)

    takeAnother = findViewById<ImageButton>(R.id.take_another_btn)

    snapCamera = findViewById<ImageButton>(R.id.snap_camera_btn)

    createCameraSource(selectedMode)

    snapCamera.setOnClickListener {

      takePicture()

      snapCamera.visibility = View.GONE
      takeAnother.visibility = View.VISIBLE
      selectImageButton.visibility = View.GONE
      processButton.visibility = View.VISIBLE

    }

    takeAnother.setOnClickListener {

      camera.close()
      cameraSource?.clearProcessor()

      camera.visibility = View.GONE

      snapCamera.visibility = View.VISIBLE
      takeAnother.visibility = View.GONE
      selectImageButton.visibility = View.VISIBLE
      processButton.visibility = View.GONE

      previewView?.visibility = View.GONE

      preview?.clearAnimation()
      preview?.clearFocus()
      preview?.visibility = View.GONE
      graphicOverlay!!.clear()
      graphicOverlay!!.clearAnimation()
      graphicOverlay!!.clearFocus()
      cameraSource = null
      preview = null
      graphicOverlay = null
      previewView = null

      finish()

      Timer().schedule(1000){
        startActivity(intent)
      }

    }

    previewView?.visibility = View.VISIBLE
    preview?.visibility = View.VISIBLE
    camera.visibility = View.GONE

    selectImageButton.visibility = View.VISIBLE
    processButton.visibility = View.GONE

    mDetector = GestureDetectorCompat(this, this)
    mDetector.setOnDoubleTapListener(this)

    CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)

    camera.setLifecycleOwner(this)
    camera.addCameraListener(Listener())

    (previewView!! as View).setOnTouchListener {

        v, e ->

      var c: Boolean = true

      when (e?.action) {

        MotionEvent.ACTION_DOWN -> {
          c = mDetector.onTouchEvent(e)
        }

        MotionEvent.ACTION_UP -> {
          c = mDetector.onTouchEvent(e)
          v!!.performClick()
        }
        else -> {

        }

      }

      c

    }

    (preview!! as View).setOnTouchListener {

        v, e ->

      var c: Boolean = true

      when (e?.action) {

        MotionEvent.ACTION_DOWN -> {
          c = mDetector.onTouchEvent(e)
        }

        MotionEvent.ACTION_UP -> {
          c = mDetector.onTouchEvent(e)
          v!!.performClick()
        }
        else -> {

        }

      }

      c

    }

    if (USE_FRAME_PROCESSOR) {

      camera.addFrameProcessor(object : FrameProcessor {

        private var lastTime = System.currentTimeMillis()

        override fun process(frame: Frame) {

          val newTime = frame.time

          val delay = newTime - lastTime
          lastTime = newTime

          LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)

          if (DECODE_BITMAP) {

            if (frame.format == ImageFormat.NV21
              && frame.dataClass == ByteArray::class.java
            ) {

              val data = frame.getData<ByteArray>()

              val yuvImage = YuvImage(
                data,
                frame.format,
                frame.size.width,
                frame.size.height,
                null
              )

              val jpegStream = ByteArrayOutputStream()
              yuvImage.compressToJpeg(
                Rect(
                  0, 0,
                  frame.size.width,
                  frame.size.height
                ),
                100, jpegStream
              )

              val jpegByteArray = jpegStream.toByteArray()

              val bitmap = BitmapFactory.decodeByteArray(
                jpegByteArray,
                0, jpegByteArray.size
              )

              bitmap.toString()

            }

          }

        }

      })

    }

    processButton.setOnClickListener {

      process()

    }

//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

    populateFeatureSelector()
    populateSizeSelector()

    isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (savedInstanceState != null) {
      imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI)
      imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH)
      imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT)
      selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE)
    }

    val rootView = findViewById<View>(R.id.root)
    rootView.viewTreeObserver.addOnGlobalLayoutListener(
      object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
          imageMaxWidth = rootView.width
          imageMaxHeight =
            rootView.height - findViewById<View>(R.id.control).height
          if (SIZE_SCREEN == selectedSize) {
            tryReloadAndDetectInImage()
          }
        }
      })

    live_settings_ll.setOnClickListener {

      val intent =
        Intent(applicationContext, SettingsActivity::class.java)

      intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.STILL_IMAGE)

      startActivity(intent)

    }

  }

  /** Stops the camera. */
  override fun onPause() {
    super.onPause()
    previewView?.stop()
  }

  public override fun onResume() {
    super.onResume()

    Log.e(TAG, "onResume")
    if (imageUri != null) {
      createImageProcessor()
      tryReloadAndDetectInImage()
    }
    else {
      createCameraSource(selectedMode)
      startCameraSource()
    }
  }

  private fun populateFeatureSelector() {

    val featureSpinner = findViewById<Spinner>(R.id.feature_selector)

    val options: MutableList<String> = ArrayList()
    options.add(TEXT_RECOGNITION)
    options.add(IMAGE_LABELING)
    options.add(POSE_DETECTION)
    options.add(FACE_DETECTION)
    options.add(SELFIE_SEGMENTATION)
    options.add(OBJECT_DETECTION)
    options.add(BARCODE_SCANNING)
//    options.add(OBJECT_DETECTION_CUSTOM)
//    options.add(CUSTOM_AUTOML_OBJECT_DETECTION)
//    options.add(IMAGE_LABELING_CUSTOM)
//    options.add(CUSTOM_AUTOML_LABELING)

    val dataAdapter = ArrayAdapter(this, R.layout.vision_spinner_style, options)

    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    featureSpinner.adapter = dataAdapter

    featureSpinner.onItemSelectedListener = object : OnItemSelectedListener {

      override fun onItemSelected(
        parentView: AdapterView<*>,
        selectedItemView: View?,
        pos: Int,
        id: Long
      ) {

        if (pos >= 0) {

          selectedMode = parentView.getItemAtPosition(pos).toString()
          Log.e(TAG, "Selected mode: $selectedMode")

          createCameraSource(selectedMode)
          startCameraSource()

          createImageProcessor()
          tryReloadAndDetectInImage()

        }

      }

      override fun onNothingSelected(arg0: AdapterView<*>?) {}

    }

  }

  private fun populateSizeSelector() {

    val sizeSpinner = findViewById<Spinner>(R.id.size_selector)
    val options: MutableList<String> = ArrayList()
    options.add(SIZE_SCREEN)
    options.add(SIZE_1024_768)
    options.add(SIZE_640_480)

    val dataAdapter =
      ArrayAdapter(this, R.layout.vision_spinner_style, options)

    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    sizeSpinner.adapter = dataAdapter
    sizeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parentView: AdapterView<*>,
        selectedItemView: View?,
        pos: Int,
        id: Long
      ) {
        if (pos >= 0) {
          selectedSize = parentView.getItemAtPosition(pos).toString()
          createImageProcessor()
          tryReloadAndDetectInImage()
        }
      }

      override fun onNothingSelected(arg0: AdapterView<*>?) {}
    }
  }

  public override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putParcelable(
      KEY_IMAGE_URI,
      imageUri
    )

    outState.putInt(
      KEY_IMAGE_MAX_WIDTH,
      imageMaxWidth
    )

    outState.putInt(
      KEY_IMAGE_MAX_HEIGHT,
      imageMaxHeight
    )

    outState.putString(
      KEY_SELECTED_SIZE,
      selectedSize
    )

  }

  private fun startCameraIntentForResult() {

    imageUri = null
    preview!!.setImageBitmap(null)
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (takePictureIntent.resolveActivity(packageManager) != null) {
      val values = ContentValues()
      values.put(MediaStore.Images.Media.TITLE, "New Picture")
      values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
      imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

      startActivityForResult(
        takePictureIntent,
        REQUEST_IMAGE_CAPTURE)

    }

  }

  private fun startChooseImageIntentForResult() {
    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT
    startActivityForResult(
      Intent.createChooser(intent, "Select Picture"),
      REQUEST_CHOOSE_IMAGE
    )
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {

    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

      tryReloadAndDetectInImage()

    }
    else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {

      imageUri = data!!.data
      tryReloadAndDetectInImage()

    }
    else {
      super.onActivityResult(requestCode, resultCode, data)
    }

  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {

    activity.window?.let {

        window ->

      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      val locationOfViewInWindow = IntArray(2)
      view.getLocationInWindow(locationOfViewInWindow)

      try {

        PixelCopy.request(
          window,
          Rect(
            locationOfViewInWindow[0], locationOfViewInWindow[1],
            locationOfViewInWindow[0] + view.width,
            locationOfViewInWindow[1] + view.height
          ),

          bitmap, { copyResult ->

            if (copyResult == PixelCopy.SUCCESS) {
              callback(bitmap)
            }

          }, Handler()
        )

      }
      catch (e: IllegalArgumentException) {

        e.printStackTrace()

      }
    }

  }

  private lateinit var imageBitmap: Bitmap


  private fun setBitmap(scale: Boolean) {

    val targetedSize = targetedWidthHeight

    val scaleFactor = max(
      imageBitmap.width.toFloat() / targetedSize.first.toFloat(),
      imageBitmap.height.toFloat() / targetedSize.second.toFloat())

    val resizedBitmap = Bitmap.createScaledBitmap(
      imageBitmap,
      (imageBitmap.width / scaleFactor).toInt(),
      (imageBitmap.height / scaleFactor).toInt(),
      true
    )

    val height = context.resources.displayMetrics.heightPixels
    val width = context.resources.displayMetrics.widthPixels + 80

    val reSized = getResizedBitmap(imageBitmap, height, width)!!

    if (!scale) {

      preview!!.setImageBitmap(reSized)

    }
    else {

      preview!!.setImageBitmap(resizedBitmap)

    }

    snapCamera.visibility = View.GONE
    takeAnother.visibility = View.VISIBLE
    selectImageButton.visibility = View.GONE
    processButton.visibility = View.VISIBLE


    if (imageProcessor != null) {

      Log.e(TAG, "imageProcessor != null")

      if (!scale) {

        graphicOverlay!!
          .setImageSourceInfo(
            reSized.width,
            reSized.height,
            false)

        imageProcessor!!.processBitmap(reSized, graphicOverlay)

      }
      else {

        graphicOverlay!!.setImageSourceInfo(
          resizedBitmap.width,
          resizedBitmap.height,
          false)

        imageProcessor!!.processBitmap(resizedBitmap, graphicOverlay)

      }

    }
    else {

      Log.e(TAG, "Null imageProcessor, please check adb logs for imageProcessor creation error")

    }

  }

  private fun tryReloadAndDetectInImage(): Unit {

    Log.e(TAG, "Try reload and detect image")

    try {

      if (imageUri == null && !didTakePicture) {

        Log.e("ATTENTION ATTENTION", "Is this the source of all my problems 1?")

        return
      }

      if (SIZE_SCREEN == selectedSize && imageMaxWidth == 0) {

        Log.e("ATTENTION ATTENTION", "Is this the source of all my problems 2?")
        return

      }


//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

      try {
        graphicOverlay?.clear()
      }
      catch (e: Exception) {

      }


      if (didTakePicture) {

        preview?.visibility = View.VISIBLE
        previewView?.visibility = View.INVISIBLE
        graphicOverlay?.visibility = View.VISIBLE
        controlLayout?.visibility = View.VISIBLE
        camera.close()
        camera.visibility = View.INVISIBLE

        didTakePicture = false

        Log.e(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.O: getBitmapFromView ran")

        setBitmap(false)

      }
      else {

        previewView?.stop()
        previewView?.release()
        previewView?.visibility = View.GONE
        preview?.visibility = View.VISIBLE
        imageBitmap = BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri) ?:return
        setBitmap(true)

      }


//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------



    }
    catch (e: IOException) {

      Log.e(TAG, "Error retrieving saved image")
      imageUri = null

    }

  }

  private val targetedWidthHeight: Pair<Int, Int>
    get() {
      val targetWidth: Int
      val targetHeight: Int
      when (selectedSize) {
        SIZE_SCREEN -> {
          targetWidth = imageMaxWidth
          targetHeight = imageMaxHeight
        }
        SIZE_640_480 -> {
          targetWidth = if (isLandScape) 640 else 480
          targetHeight = if (isLandScape) 480 else 640
        }
        SIZE_1024_768 -> {
          targetWidth = if (isLandScape) 1024 else 768
          targetHeight = if (isLandScape) 768 else 1024
        }
        else -> throw IllegalStateException("Unknown size")
      }
      return Pair(targetWidth, targetHeight)
    }

  private fun createImageProcessor() {

    try {

      when (selectedMode) {

        OBJECT_DETECTION -> {

          Log.i(TAG, "Using Object Detector Processor")

          val objectDetectorOptions = PreferenceUtils
            .getObjectDetectorOptionsForStillImage(this)

          imageProcessor = ObjectDetectorProcessor(this, objectDetectorOptions)

        }
        OBJECT_DETECTION_CUSTOM -> {

          Log.i(TAG, "Using Custom Object Detector Processor")

          val localModel = LocalModel.Builder()
            .setAssetFilePath("custom_models/bird_classifier.tflite")
            .build()

          val customObjectDetectorOptions = PreferenceUtils
            .getCustomObjectDetectorOptionsForStillImage(this, localModel)

          imageProcessor = ObjectDetectorProcessor(this, customObjectDetectorOptions)

        }
        CUSTOM_AUTOML_OBJECT_DETECTION -> {

          Log.i(TAG, "Using Custom AutoML Object Detector Processor")

          val customAutoMLODTLocalModel = LocalModel.Builder()
            .setAssetManifestFilePath("automl/manifest.json")
            .build()

          val customAutoMLODTOptions = PreferenceUtils
            .getCustomObjectDetectorOptionsForStillImage(this, customAutoMLODTLocalModel)

          imageProcessor = ObjectDetectorProcessor(this, customAutoMLODTOptions)

        }
        TEXT_RECOGNITION -> imageProcessor = TextRecognitionProcessor(this, TextRecognizerOptions.Builder().build())
        FACE_DETECTION -> imageProcessor = FaceDetectorProcessor(this, null)
        BARCODE_SCANNING -> imageProcessor = BarcodeScannerProcessor(this)
        IMAGE_LABELING -> imageProcessor = LabelDetectorProcessor(
          this,
          ImageLabelerOptions.DEFAULT_OPTIONS
        )
        IMAGE_LABELING_CUSTOM -> {

          Log.i(TAG, "Using Custom Image Label Detector Processor")

          val localClassifier = LocalModel.Builder()
            .setAssetFilePath("custom_models/bird_classifier.tflite")
            .build()

          val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localClassifier).build()

          imageProcessor = LabelDetectorProcessor(this, customImageLabelerOptions)

        }
        CUSTOM_AUTOML_LABELING -> {

          Log.i(TAG, "Using Custom AutoML Image Label Detector Processor")

          val customAutoMLLabelLocalModel = LocalModel.Builder()
            .setAssetManifestFilePath("automl/manifest.json")
            .build()

          val customAutoMLLabelOptions = CustomImageLabelerOptions
            .Builder(customAutoMLLabelLocalModel)
            .setConfidenceThreshold(0f)
            .build()

          imageProcessor = LabelDetectorProcessor(this, customAutoMLLabelOptions)

        }
        POSE_DETECTION -> {

          val poseDetectorOptions = PreferenceUtils
            .getPoseDetectorOptionsForStillImage(this)

          Log.i(TAG, "Using Pose Detector with options $poseDetectorOptions")

          val shouldShowInFrameLikelihood = PreferenceUtils
            .shouldShowPoseDetectionInFrameLikelihoodStillImage(this)
          val visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)
          val rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)
          val runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this)

          imageProcessor = PoseDetectorProcessor(
            this, poseDetectorOptions, shouldShowInFrameLikelihood, visualizeZ, rescaleZ,
            runClassification, /* isStreamMode = */ false
          )

        }
        SELFIE_SEGMENTATION -> {

          imageProcessor = SegmenterProcessor(this, /* isStreamMode= */ false)

        }
        else -> Log.e(TAG, "Unknown selectedMode: $selectedMode")

      }

    }
    catch (e: Exception) {

      Log.e(TAG, "Can not create image processor: $selectedMode", e)

      Toast.makeText(
        applicationContext,
        "Can not create image processor: " + e.message,
        Toast.LENGTH_LONG
      )
        .show()

    }

  }


  @Throws(IOException::class)
  private fun save(bytes: ByteArray, file: File) {

    var output: OutputStream? = null

    try {
      output = FileOutputStream(file)
      output.write(bytes)
    } finally {
      output?.close()
    }


  }

  fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap? {

    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height

    val matrix = Matrix()

    matrix.postScale(scaleWidth, scaleHeight)

    return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)

  }

  private inner class Listener : CameraListener() {

    override fun onCameraOpened(options: CameraOptions) {}

    override fun onCameraError(exception: CameraException) {
      super.onCameraError(exception)
    }

    override fun onPictureTaken(result: PictureResult) {
      super.onPictureTaken(result)

      if (camera.isTakingVideo) {
        message("Captured while taking video. Size=" + result.size, false)
        return
      }

      controlLayout?.visibility = View.VISIBLE


      val callbackTime = System.currentTimeMillis()

      Log.e("ATTENTION ATTENTION", "onPictureTaken called!")

      result.toBitmap {

          bitmap ->

        val currentTimeMillis = System.currentTimeMillis()

        val dateFormat: DateFormat = getDateTimeInstance() //SimpleDateFormat("yy/MM/dd HH:mm:ss:SSS")

        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = Date(currentTimeMillis)

        val currentTime = dateFormat.format(date)

        val myFile = "$currentTime.jpg"

        val file =
          File(context.getExternalFilesDir(null).toString() +
                  File.separator + "Recharge Folder" + "/" + myFile)

        try {
          save(result.data, file)
        }
        catch (e: IOException) {
          e.printStackTrace()
        }

        val height = context.resources.displayMetrics.heightPixels
        val width = context.resources.displayMetrics.widthPixels

        imageBitmap = getResizedBitmap(bitmap!!, height, width)!!

        createImageProcessor()
        tryReloadAndDetectInImage()

        Log.e("ATTENTION ATTENTION", "onPictureTaken called! Worked.")

      }

      captureTime = 0

    }

    override fun onVideoTaken(result: VideoResult) {
      super.onVideoTaken(result)

    }

    override fun onVideoRecordingStart() {
      super.onVideoRecordingStart()
      LOG.w("onVideoRecordingStart!")
    }

    override fun onVideoRecordingEnd() {
      super.onVideoRecordingEnd()
      LOG.w("onVideoRecordingEnd!")
    }

    override fun onExposureCorrectionChanged(
      newValue: Float,
      bounds: FloatArray,
      fingers: Array<PointF>?
    ) {
      super.onExposureCorrectionChanged(newValue, bounds, fingers)
    }

    override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
      super.onZoomChanged(newValue, bounds, fingers)
    }

  }

  private fun capturePicture() {
    if (camera.mode == Mode.VIDEO) return run {
      message("Can't take HQ pictures while in VIDEO mode.", false)
    }
    if (camera.isTakingPicture) return
    captureTime = System.currentTimeMillis()
    message("Capturing picture...", false)
    camera.takePicture()
  }

  private fun capturePictureSnapshot() {
    if (camera.isTakingPicture) return
    if (camera.preview != Preview.GL_SURFACE) return run {
      message("Picture snapshots are only allowed with the GL_SURFACE preview.", true)
    }
    captureTime = System.currentTimeMillis()
    message("Capturing picture snapshot...", false)
    camera.takePictureSnapshot()
  }

  private fun captureVideo() {
    if (camera.mode == Mode.PICTURE) return run {
      message("Can't record HQ videos while in PICTURE mode.", false)
    }
    if (camera.isTakingPicture || camera.isTakingVideo) return
    message("Recording for 5 seconds...", true)
    camera.takeVideo(File(filesDir, "video.mp4"), 5000)
  }

  private fun captureVideoSnapshot() {
    if (camera.isTakingVideo) return run {
      message("Already taking video.", false)
    }
    if (camera.preview != Preview.GL_SURFACE) return run {
      message("Video snapshots are only allowed with the GL_SURFACE preview.", true)
    }
    message("Recording snapshot for 5 seconds...", true)
    camera.takeVideoSnapshot(File(filesDir, "video.mp4"), 5000)
  }

  private fun toggleCamera() {
    if (camera.isTakingPicture || camera.isTakingVideo) return
    when (camera.toggleFacing()) {
      Facing.BACK -> message("Switched to back camera!", false)
      Facing.FRONT -> message("Switched to front camera!", false)
      null -> message("Switch Null", false)
    }
  }

  private fun changeCurrentFilter() {

    if (camera.preview != Preview.GL_SURFACE) return run {
      message("Filters are supported only when preview is Preview.GL_SURFACE.", true)
    }

    if (currentFilter < allFilters.size - 1) {
      currentFilter++
    }
    else {
      currentFilter = 0
    }

    val filter = allFilters[currentFilter]
    message(filter.toString(), false)

    camera.filter = filter.newInstance()

  }


  companion object {


    private val LOG = CameraLogger.create("DemoApp")
    private const val USE_FRAME_PROCESSOR = false
    private const val DECODE_BITMAP = false


    private const val TAG = "RecogActivityFinal"
    private const val OBJECT_DETECTION = "Object Detection"
    private const val OBJECT_DETECTION_CUSTOM = "Custom Object Detection (Birds)"
    private const val CUSTOM_AUTOML_OBJECT_DETECTION = "Custom AutoML Object Detection (Flower)"
    private const val FACE_DETECTION = "Face Detection"
    private const val BARCODE_SCANNING = "Barcode Scanning"
    private const val TEXT_RECOGNITION = "Text Recognition"
    private const val IMAGE_LABELING = "Image Labeling"
    private const val IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)"
    private const val CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)"
    private const val POSE_DETECTION = "Pose Detection"
    private const val SELFIE_SEGMENTATION = "Selfie Segmentation"

    private const val SIZE_SCREEN = "w:screen" // Match screen width
    private const val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
    private const val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio
    private const val KEY_IMAGE_URI = "com.ej.recharge.vision.KEY_IMAGE_URI"
    private const val KEY_IMAGE_MAX_WIDTH = "com.ej.recharge.vision.KEY_IMAGE_MAX_WIDTH"
    private const val KEY_IMAGE_MAX_HEIGHT = "com.ej.recharge.vision.KEY_IMAGE_MAX_HEIGHT"
    private const val KEY_SELECTED_SIZE = "com.ej.recharge.vision.KEY_SELECTED_SIZE"
    private const val REQUEST_IMAGE_CAPTURE = 1001
    private const val REQUEST_CHOOSE_IMAGE = 1002

    private const val PERMISSION_REQUESTS = 1

    private fun isPermissionGranted(
      context: Context,
      permission: String?
    ): Boolean {

      if (ContextCompat.checkSelfPermission(context, permission!!) ==
        PackageManager.PERMISSION_GRANTED) {

        Log.i(TAG, "Permission granted: $permission")

        return true

      }

      Log.i(TAG, "Permission NOT granted: $permission")

      return false

    }


  }




}
