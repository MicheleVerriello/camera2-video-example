import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraView() {

    val context = LocalContext.current

    // FlashLight
    val isFlashOn = remember { mutableStateOf(false) }
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager // initializing our camera manager.

    // Image analysis
    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    val analyzerUseCase = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetResolution(Size(620, 480))
        //.setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .build()

    var i = 0

    analyzerUseCase.setAnalyzer(executor) { imageProxy ->

        imageProxy.close()
    }


    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    LaunchedEffect(lensFacing) {// this will run every time we change the lensFacing
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            analyzerUseCase
        )

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
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CameraFlipToggleButton(lensFacing) {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            }
            Spacer(modifier = Modifier.width(30.dp))

            FlashlightToggleButton() {
                isFlashOn.value = !isFlashOn.value
                enableFlashlight(cameraManager, isFlashOn.value)
            }
        }
    }
}

// Widget camera flip
@Composable
private fun CameraFlipToggleButton(lensFacing: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.FlipCameraAndroid,
                contentDescription = "Flip camera",
                modifier = Modifier.size(32.dp)
            )
            Text(if (lensFacing == CameraSelector.LENS_FACING_FRONT) "Use back camera" else "Use front camera")
        }
    }
}

// Widget for flashlight button
@Composable
private fun FlashlightToggleButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.FlashlightOn,
                contentDescription = "Flashlight",
                modifier = Modifier.size(32.dp)
            )
            Text("Flashlight")
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

    val cameraId = cameraManager.cameraIdList[0]
    try {
        cameraManager.setTorchMode(cameraId, enable)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}