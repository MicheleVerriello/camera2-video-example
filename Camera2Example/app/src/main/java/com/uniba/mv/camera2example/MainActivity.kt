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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.uniba.mv.camera2example.ui.theme.Camera2ExampleTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    // Ambient light sensor
    private lateinit var sensorManager: SensorManager
    private var ambientLightSensor: Sensor? = null
    private var ambientLightValue = mutableStateOf(0f)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
        } else {
            Log.i("kilo", "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Camera2ExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
                    MainView(ambientLightValue)
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
        if (p0 != null) {
            Log.i("onSensorChanged", "p0.values size = ${p0.values.size}")
            Log.i("onSensorChanged", "p0.values = ${p0.values[0]}")
            ambientLightValue.value = p0.values[0]
        } else {
            Log.i("onSensorChanged", "p0 is null")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i("onAccuracyChanged", "p0 = ${p0.toString()}")
        Log.i("onAccuracyChanged", "p1 = $p1")
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA ) ->
                Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun MainView(ambientLightValue: MutableState<Float>) {

    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
        ){
            Row {
                Text(color = Color.Black, text = "Ambient Light \t")
                Text(color = Color.Black, text = ambientLightValue.value.toString())
                Text(color = Color.Black, text = "\t lx")
            }
        }

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CameraView()
        }
    }
}