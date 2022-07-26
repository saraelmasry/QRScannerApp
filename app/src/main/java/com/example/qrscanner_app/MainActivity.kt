package com.example.qrscanner_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object{
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
//1
      //private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var textureView: TextureView
    private lateinit var qrresp: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView = findViewById(R.id.texture_view)

//2
         //val bottomSheet:View=findViewById(R.id.persistent_bottom_sheet)
        // bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Request camera permissions
        if (isCameraPermissionGranted()) {
            textureView.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }
    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder()
            // We want to show input from back camera of the device
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            textureView.setSurfaceTexture(previewOutput.surfaceTexture)
            parent.addView(textureView, 0)
        }

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)

        val qrCodeAnalyzer = QrCodeAnalyzer { qrCodes ->
            qrCodes.forEach {
                qrresp = it.rawValue!!.toLowerCase()

                qr_resolve.text = qrresp
                if(qrresp.contains("http")){
                    qr_resolve.movementMethod = LinkMovementMethod.getInstance()
                }
            }
        }

        imageAnalysis.analyzer = qrCodeAnalyzer

        // We need to bind preview and imageAnalysis use cases
        CameraX.bindToLifecycle(this as LifecycleOwner, preview, imageAnalysis)
    }
    private fun isCameraPermissionGranted(): Boolean {
        val selfPermission =
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
        return selfPermission == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (isCameraPermissionGranted()) {
                textureView.post { startCamera() }
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}