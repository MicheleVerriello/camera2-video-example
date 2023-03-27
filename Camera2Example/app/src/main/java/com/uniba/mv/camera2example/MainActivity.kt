package com.uniba.mv.camera2example

import CameraView
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.CONTOUR_MODE_ALL
import com.google.mlkit.vision.face.FaceDetectorOptions.ContourMode
import com.uniba.mv.camera2example.ui.theme.Camera2ExampleTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(), SensorEventListener {

    // Ambient light sensor
    private lateinit var sensorManager: SensorManager
    private var ambientLightSensor: Sensor? = null
    private var ambientLightValue = mutableStateOf(0f)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("Permissions", "Permission granted")
        } else {
            Log.i("Permissions", "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()


        // Real-time contour detection
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(CONTOUR_MODE_ALL)
            .build()


        val analyzerUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // Image analysis
        val executor: ExecutorService = Executors.newSingleThreadExecutor()

        val detector = FaceDetection.getClient(realTimeOpts)


        setContent {
            Camera2ExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
                    MainView(ambientLightValue, analyzerUseCase, executor, detector)
                }
            }
        }

        requestCameraPermission()

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
        sensorManager.registerListener(this, ambientLightSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null)
            ambientLightValue.value = p0.values[0]
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {}

            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA ) -> {}

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun MainView(ambientLightValue: MutableState<Float>, analyzerUseCase: ImageAnalysis, executor: ExecutorService, detector: FaceDetector) {

    Column {
        Box(modifier = Modifier .fillMaxWidth()){
            Row {
                Text(color = Color.Black, text = "Ambient Light \t")
                Text(color = Color.Black, text = ambientLightValue.value.toString())
                Text(color = Color.Black, text = "\t lx")
            }
        }

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CameraView(analyzerUseCase, executor, detector)
        }
    }
}