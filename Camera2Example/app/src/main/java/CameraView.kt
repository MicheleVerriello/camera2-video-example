import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraView() {

    val context = LocalContext.current

    // FlashLight
    val isFlashOn = remember { mutableStateOf(false) }
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager // initializing our camera manager.



    var lensFacing = CameraSelector.LENS_FACING_FRONT
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview = Preview.Builder().build()
    var previewView = remember { PreviewView(context) }
    var cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        AndroidView(
            { previewView },
            modifier = Modifier
                .width(250.dp)
                .height(280.dp)
                .clip(RoundedCornerShape(1000.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            IconButton(
                onClick = {

                    var actualCameraId = if(lensFacing == 0) "1" else "0"
                    flipCamera(cameraManager, actualCameraId, stateCallback, context)
                }
            ) {
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        modifier = Modifier.size(32.dp)
                    )
                    Text("Flip camera")
                }
            }

            IconButton(onClick = {
                isFlashOn.value = !isFlashOn.value
                enableFlashlight(cameraManager, isFlashOn.value)
            }) {
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FlashlightOn,
                        contentDescription = "Flashligh",
                        modifier = Modifier.size(32.dp)
                    )
                    Text("Flashlight")
                }
            }
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

private fun enableFlashlight(cameraManager: CameraManager, enable: Boolean) {

    // creating a string for camera ID
    lateinit var cameraID: String

    try {
        // O means back camera unit, 1 means front camera unit
        // get camera id for back camera as we will be using torch for back camera
        cameraID = cameraManager.cameraIdList[0]

        if(enable) {
            cameraManager.setTorchMode(cameraID, true)
        } else {
            cameraManager.setTorchMode(cameraID, false)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun flipCamera(cameraManager: CameraManager, actualCameraId: String, stateCallback: CameraDevice.StateCallback, context: Context) {
    cameraManager.cameraIdList
    Log.i("flipCamera", "Actual camera is $actualCameraId")
    Log.i("flipCamera", "Camera id List")
    for(cam in cameraManager.cameraIdList) {
        Log.i("flipCamera", cam)
    }

    val cameraId = if(actualCameraId == "1") "0" else "1"
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
        Toast.makeText(context, "Cannot open camera", Toast.LENGTH_SHORT).show()
    } else {
        cameraManager.openCamera(cameraId, stateCallback, null)
    }
}