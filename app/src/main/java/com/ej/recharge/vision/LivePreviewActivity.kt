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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ej.recharge.R
import com.ej.recharge.vision.barcodescanner.BarcodeScannerProcessor
import com.ej.recharge.vision.facedetector.FaceDetectorProcessor
import com.ej.recharge.vision.labeldetector.LabelDetectorProcessor
import com.ej.recharge.vision.objectdetector.ObjectDetectorProcessor
import com.ej.recharge.vision.posedetector.PoseDetectorProcessor
import com.ej.recharge.vision.preference.PreferenceUtils
import com.ej.recharge.vision.preference.SettingsActivity
import com.ej.recharge.vision.preference.SettingsActivity.LaunchSource
import com.ej.recharge.vision.segmenter.SegmenterProcessor
import com.ej.recharge.vision.textdetector.TextRecognitionProcessor
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.vision_activity_vision_live_preview.*
import java.io.IOException
import java.util.*

/** Live preview demo for ML Kit APIs.  */
@KeepName
class LivePreviewActivity : AppCompatActivity(),
  ActivityCompat.OnRequestPermissionsResultCallback,
  OnItemSelectedListener,
  CompoundButton.OnCheckedChangeListener {

  private var selectedMode = OBJECT_DETECTION
  private var cameraSource: CameraSource? = null
  private var previewView: CameraSourcePreview? = null
  private var graphicOverlay: GraphicOverlay? = null









//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------


//  fun takePicture() {
//
//    cameraSource?.camera?.takePicture(null, null, object : Camera.PictureCallback {
//
//      override fun onPictureTaken(bytes: ByteArray?, camera: Camera?) {
//
//        this@Camera1Manager.onPictureTaken(bytes, camera, callback)
//
//      }
//
//    })
//
//  }




















//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO-------------------------------------After Lekweuwa-----------------------------------------
//  TODO--------------------------------------------------------------------------------------------
//  TODO--------------------------------------------------------------------------------------------















  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Log.d(TAG, "onCreate")

    setContentView(R.layout.vision_activity_vision_live_preview)

    previewView = findViewById(R.id.preview_view)

    if (previewView == null) {
      Log.d(TAG, "Preview is null")
    }

    graphicOverlay = findViewById(R.id.graphic_overlay)

    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null")
    }

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

    // Creating adapter for spinner
    val dataAdapter = ArrayAdapter(this, R.layout.vision_spinner_style, options)

    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    // attaching data adapter to spinner
    spinner.adapter = dataAdapter
    spinner.onItemSelectedListener = this

    val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
    facingSwitch.setOnCheckedChangeListener(this)

//    val settingsButton = findViewById<ImageView>(R.id.live_settings_button)
    live_settings_button.setOnClickListener {
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

  }

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

          cameraSource!!.setMachineLearningFrameProcessor(TextRecognitionProcessor(this, TextRecognizerOptions.Builder().build()))

        }
        FACE_DETECTION -> {

          Log.i(TAG, "Using Face Detector Processor")

          val faceDetectorOptions =
            PreferenceUtils.getFaceDetectorOptions(this)

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

  public override fun onResume() {
    super.onResume()
    Log.d(TAG, "onResume")
    createCameraSource(selectedMode)
    startCameraSource()
  }

  /** Stops the camera.  */
  override fun onPause() {
    super.onPause()
    previewView?.stop()
  }

  public override fun onDestroy() {
    super.onDestroy()
    if (cameraSource != null) {
      cameraSource?.release()
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

  companion object {

    private const val OBJECT_DETECTION = "Object Detection"
    private const val OBJECT_DETECTION_CUSTOM = "Custom Object Detection (Birds)"
    private const val CUSTOM_AUTOML_OBJECT_DETECTION = "Custom AutoML Object Detection (Flower)"
    private const val FACE_DETECTION = "Face Detection"
    private const val TEXT_RECOGNITION = "Text Recognition"
    private const val BARCODE_SCANNING = "Barcode Scanning"
    private const val IMAGE_LABELING = "Image Labeling"
    private const val IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)"
    private const val CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)"
    private const val POSE_DETECTION = "Pose Detection"
    private const val SELFIE_SEGMENTATION = "Selfie Segmentation"

    private const val TAG = "LivePreviewActivity"
    private const val PERMISSION_REQUESTS = 1

    private fun isPermissionGranted(
      context: Context,
      permission: String?
    ): Boolean {

      if (ContextCompat.checkSelfPermission(context, permission!!)
        == PackageManager.PERMISSION_GRANTED
      ) {
        Log.i(TAG, "Permission granted: $permission")
        return true
      }
      Log.i(TAG, "Permission NOT granted: $permission")
      return false

    }

  }

}
