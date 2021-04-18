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

package com.app.ej.cs.vision

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.SensorManager.getOrientation
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.impl.utils.Exif
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.ej.cs.R
import com.app.ej.cs.utils.Util
import com.app.ej.cs.vision.barcodescanner.BarcodeScannerProcessor
import com.app.ej.cs.vision.facedetector.FaceDetectorProcessor
import com.app.ej.cs.vision.labeldetector.LabelDetectorProcessor
import com.app.ej.cs.vision.objectdetector.ObjectDetectorProcessor
import com.app.ej.cs.vision.posedetector.PoseDetectorProcessor
import com.app.ej.cs.vision.preference.PreferenceUtils
import com.app.ej.cs.vision.preference.SettingsActivity
import com.app.ej.cs.vision.preference.SettingsActivity.LaunchSource
import com.app.ej.cs.vision.segmenter.SegmenterProcessor
import com.app.ej.cs.vision.textdetector.TextRecognitionProcessor
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.vision_activity_still_image_final.*
import java.io.IOException
import java.util.*
import kotlin.math.max
import android.hardware.SensorManager.*


/** Activity demonstrating different image detector features with a still image from camera.  */
@KeepName
class RecognitionActivity : AppCompatActivity() ,
  ActivityCompat.OnRequestPermissionsResultCallback,
  OnItemSelectedListener,
  CompoundButton.OnCheckedChangeListener {

//  private var selectedModel = OBJECT_DETECTION
  private var preview: ImageView? = null
  private var graphicOverlay: GraphicOverlay? = null

  private var selectedMode = OBJECT_DETECTION
  private var selectedSize: String? = SIZE_SCREEN

  private var isLandScape = false
  private var imageUri: Uri? = null

  // Max width (portrait mode)
  private var imageMaxWidth = 0
  // Max height (portrait mode)
  private var imageMaxHeight = 0

  private var imageProcessor: VisionImageProcessor? = null

  private var cameraSource: CameraSource? = null
  private var previewView: CameraSourcePreview? = null
  private var didTakePicture: Boolean = false










  @Synchronized
  override fun onItemSelected(
    parent: AdapterView<*>?,
    view: View?,
    pos: Int,
    id: Long
  ) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selectedMode = parent?.getItemAtPosition(pos).toString()
    Log.d(TAG, "Selected mode: $selectedMode")
    previewView?.stop()
    if (allPermissionsGranted()) {
      createCameraSource(selectedMode)
      startCameraSource()
    } else {
      runtimePermissions
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    // Do nothing.
  }

  override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
    Log.d(TAG, "Set facing")
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
      } else {
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
      }
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
          Log.d(TAG, "resume: Preview is null")
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null")
        }
        previewView!!.start(cameraSource, graphicOverlay)
      } catch (e: IOException) {
        Log.e(TAG, "Unable to start camera source.", e)
        cameraSource!!.release()
        cameraSource = null
      }
    }
  }


  private fun takePicture() {


//    previewView?.drawingCache
    didTakePicture = true
//    tryReloadAndDetectInImage()



    cameraSource?.camera?.takePicture(null, null, object : Camera.PictureCallback {

      override fun onPictureTaken(bytes: ByteArray?, camera: Camera?) {



        tryReloadAndDetectInImage()


        /*this@Camera1Manager.onPictureTaken(bytes, camera, callback)

        imageUri = bytes!!.data

    tryReloadAndDetectInImage()

        val orientation: Int = Exif.getOrientation(bytes)
        val temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val picture = rotateImage(temp, orientation)
        val overlay = Bitmap.createBitmap(
          mGraphicOverlay.getWidth(),
          mGraphicOverlay.getHeight(),
          picture.config
        )
        val canvas = Canvas(overlay)*/


      }

    })

  }

  val util: Util = Util()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.vision_activity_still_image_final)

    select_image_button.setOnClickListener {

        view: View ->

        // Menu for selecting either: a) take new photo b) select from existing
        val popup = PopupMenu(this@RecognitionActivity, view)

        popup.setOnMenuItemClickListener {

            menuItem: MenuItem ->

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

//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

    previewView = findViewById(R.id.preview_view)
    previewView?.isDrawingCacheEnabled = true

    if (previewView == null) Log.d(TAG, "Preview is null")

    if (graphicOverlay == null) Log.d(TAG, "graphicOverlay is null")

    val spinner = findViewById<Spinner>(R.id.spinner)
    val options: MutableList<String> = ArrayList()
    options.add(TEXT_RECOGNITION)
    options.add(OBJECT_DETECTION)
    options.add(OBJECT_DETECTION_CUSTOM)
    options.add(CUSTOM_AUTOML_OBJECT_DETECTION)
    options.add(FACE_DETECTION)
    options.add(BARCODE_SCANNING)
    options.add(IMAGE_LABELING)
    options.add(IMAGE_LABELING_CUSTOM)
    options.add(CUSTOM_AUTOML_LABELING)
    options.add(POSE_DETECTION)
    options.add(SELFIE_SEGMENTATION)


//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------Before Change Needed-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------

    val dataAdapter = ArrayAdapter(this, R.layout.vision_spinner_style, options)

    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    spinner.adapter = dataAdapter
    spinner.onItemSelectedListener = this


//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Change Needed-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------


    val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
    facingSwitch.setOnCheckedChangeListener(this)

    live_settings.setOnClickListener {
      val intent = Intent(applicationContext, SettingsActivity::class.java)
      intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.LIVE_PREVIEW)
      startActivity(intent)
    }

    if (allPermissionsGranted()) {
      createCameraSource(selectedMode)
    }
    else {
      runtimePermissions
    }


    snap_camera_btn.setOnClickListener {
      takePicture()
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
      object : OnGlobalLayoutListener {

        override fun onGlobalLayout() {

          rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
          imageMaxWidth = rootView.width
          imageMaxHeight = rootView.height - findViewById<View>(R.id.control).height

          if (SIZE_SCREEN == selectedSize) {
            tryReloadAndDetectInImage()
          }

        }

      })

    live_settings_button_include.setOnClickListener {

      val intent = Intent(applicationContext, SettingsActivity::class.java)

      intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.STILL_IMAGE)
      startActivity(intent)

    }

  }




  private fun startCameraIntentForResult() { // Clean up last time's image

    imageUri = null
    preview!!.setImageBitmap(null)
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    if (takePictureIntent.resolveActivity(packageManager) != null) {

      val values = ContentValues()
      values.put(MediaStore.Images.Media.TITLE, "New Picture")
      values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
      imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

    }

  }

  private fun startChooseImageIntentForResult() {

    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT

    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)

  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {

    if (requestCode == REQUEST_IMAGE_CAPTURE &&
      resultCode == Activity.RESULT_OK) {
      tryReloadAndDetectInImage()
    }
    else if (requestCode == REQUEST_CHOOSE_IMAGE &&
      resultCode == Activity.RESULT_OK) {
      // In this case, imageUri is returned by the chooser, save it.
      imageUri = data!!.data
      tryReloadAndDetectInImage()
    }
    else {
      super.onActivityResult(requestCode, resultCode, data)
    }

  }

  private fun tryReloadAndDetectInImage() {

    Log.d(TAG, "Try reload and detect image")

    try {

      if (imageUri == null && !didTakePicture) {
        return
      }

      if (SIZE_SCREEN == selectedSize && imageMaxWidth == 0) {
        // UI layout has not finished yet, will reload once it's ready.
        return
      }

//      pictureByteArray

      val imageBitmap: Bitmap =

        if (didTakePicture) {
          val message: String = "Picture Taken Camera Clicked"
          util.onShowMessage(message, this)
          previewView?.visibility = View.VISIBLE
          preview?.visibility = View.GONE
          didTakePicture = false
          previewView?.drawingCache!!
//          BitmapFactory.decodeByteArray(pictureByteArray, 0, pictureByteArray!!.size)
        }
        else {
          val message: String = "Picture Chosen"
          util.onShowMessage(message, this)
          previewView?.visibility = View.GONE
          preview?.visibility = View.VISIBLE
          BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri) ?:return
        }

      imageUri = null

      // Clear the overlay first
      graphicOverlay!!.clear()

      // Get the dimensions of the image view
      val targetedSize = targetedWidthHeight
      // Determine how much to scale down the image

      val scaleFactor = max(
        imageBitmap.width.toFloat() / targetedSize.first.toFloat(),
        imageBitmap.height.toFloat() / targetedSize.second.toFloat()
      )

      val resizedBitmap = Bitmap.createScaledBitmap(
        imageBitmap,
        (imageBitmap.width / scaleFactor).toInt(),
        (imageBitmap.height / scaleFactor).toInt(),
        true
      )

      preview!!.setImageBitmap(resizedBitmap)

      if (imageProcessor != null) {

//        val message: String = "Should Recognize on Image"
//        util.onShowMessage(message, this)
        Log.e("ATTENTION ATTENTION", "Should Recognize on Image")

        graphicOverlay!!.setImageSourceInfo(
          resizedBitmap.width,
          resizedBitmap.height, /* isFlipped= */
          false)

        imageProcessor!!.processBitmap(resizedBitmap, graphicOverlay)
//        previewView!!.start(cameraSource, graphicOverlay)

      }
      else {

        Log.e(TAG, "Null imageProcessor, please check adb logs for imageProcessor creation error")

      }

    }
    catch (e: IOException) {
      Log.e(TAG, "Error retrieving saved image")
      imageUri = null
    }

  }









  private fun createCameraSource(model: String) {

    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = CameraSource(this, graphicOverlay)
    }

    try {

      when (model) {

        OBJECT_DETECTION -> {

          Log.i(TAG, "Using Object Detector Processor")

          val objectDetectorOptions = PreferenceUtils.getObjectDetectorOptionsForLivePreview(this)

          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(this, objectDetectorOptions)
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
            ObjectDetectorProcessor(this, customObjectDetectorOptions)
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
            ObjectDetectorProcessor(this, customAutoMLODTOptions)
          )

        }
        TEXT_RECOGNITION -> {

          Log.i(TAG, "Using on-device Text recognition Processor")

          cameraSource!!.setMachineLearningFrameProcessor(TextRecognitionProcessor(this))

        }
        FACE_DETECTION -> {

          Log.i(TAG, "Using Face Detector Processor")

          val faceDetectorOptions =
            PreferenceUtils.getFaceDetectorOptionsForLivePreview(this)

          cameraSource!!.setMachineLearningFrameProcessor(
            FaceDetectorProcessor(this, faceDetectorOptions)
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
            LabelDetectorProcessor(this, ImageLabelerOptions.DEFAULT_OPTIONS)
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
            LabelDetectorProcessor(this, customImageLabelerOptions)
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
            LabelDetectorProcessor(this, customAutoMLLabelOptions)
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




  private val requiredPermissions: Array<String?>
    get() = try {
      val info = this.packageManager
        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
      val ps = info.requestedPermissions
      if (ps != null && ps.isNotEmpty()) {
        ps
      } else {
        arrayOfNulls(0)
      }
    } catch (e: Exception) {
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

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    Log.i(TAG, "Permission granted!")
    if (allPermissionsGranted()) {
      createCameraSource(selectedMode)
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }
  
  











  public override fun onResume() {
    super.onResume()
    Log.d(TAG, "onResume")
    createImageProcessor()
    tryReloadAndDetectInImage()
  }

  private fun populateFeatureSelector() {

    val featureSpinner = findViewById<Spinner>(R.id.feature_selector)
    val options: MutableList<String> = ArrayList()

    options.add(OBJECT_DETECTION)
    options.add(OBJECT_DETECTION_CUSTOM)
    options.add(CUSTOM_AUTOML_OBJECT_DETECTION)
    options.add(FACE_DETECTION)
    options.add(BARCODE_SCANNING)
    options.add(TEXT_RECOGNITION)
    options.add(IMAGE_LABELING)
    options.add(IMAGE_LABELING_CUSTOM)
    options.add(CUSTOM_AUTOML_LABELING)
    options.add(POSE_DETECTION)
    options.add(SELFIE_SEGMENTATION)

    // Creating adapter for featureSpinner
    val dataAdapter =
      ArrayAdapter(this, R.layout.vision_spinner_style, options)

    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    // attaching data adapter to spinner
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

    // Creating adapter for featureSpinner
    val dataAdapter =
      ArrayAdapter(this, R.layout.vision_spinner_style, options)

    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    // attaching data adapter to spinner
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

    outState.putParcelable(KEY_IMAGE_URI, imageUri)

    outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth)

    outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight)

    outState.putString(KEY_SELECTED_SIZE, selectedSize)

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

          val objectDetectorOptions =
            PreferenceUtils.getObjectDetectorOptionsForStillImage(this)
          imageProcessor = ObjectDetectorProcessor(this, objectDetectorOptions)

        }
        OBJECT_DETECTION_CUSTOM -> {

          Log.i(TAG, "Using Custom Object Detector Processor")

          val localModel = LocalModel.Builder()
            .setAssetFilePath("custom_models/bird_classifier.tflite")
            .build()

          val customObjectDetectorOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForStillImage(this, localModel)

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
        FACE_DETECTION -> imageProcessor = FaceDetectorProcessor(this, null)
        BARCODE_SCANNING -> imageProcessor = BarcodeScannerProcessor(this)
        TEXT_RECOGNITION -> imageProcessor = TextRecognitionProcessor(this)
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

          val poseDetectorOptions = PreferenceUtils.getPoseDetectorOptionsForStillImage(this)

          Log.i(TAG, "Using Pose Detector with options $poseDetectorOptions")

          val shouldShowInFrameLikelihood =
            PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodStillImage(this)

          val visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)
          val rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)
          val runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this)

          imageProcessor = PoseDetectorProcessor(
            this, poseDetectorOptions, shouldShowInFrameLikelihood,
            visualizeZ, rescaleZ, runClassification, /* isStreamMode = */ false
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

  companion object {

    private const val TAG = "StillImageActivity"
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
    private const val KEY_IMAGE_URI = "com.app.ej.cs.vision.KEY_IMAGE_URI"
    private const val KEY_IMAGE_MAX_WIDTH = "com.app.ej.cs.vision.KEY_IMAGE_MAX_WIDTH"
    private const val KEY_IMAGE_MAX_HEIGHT = "com.app.ej.cs.vision.KEY_IMAGE_MAX_HEIGHT"
    private const val KEY_SELECTED_SIZE = "com.app.ej.cs.vision.KEY_SELECTED_SIZE"
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
