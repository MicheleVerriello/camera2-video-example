import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("RestrictedApi")
@Composable
@ExperimentalGetImage
fun CameraView() {

    var progress by remember { mutableStateOf(0.0f) }
    val showProgressBar = remember { mutableStateOf(false) }

    val cameraZoomRatio = remember { mutableStateOf(1f) }
    val boxBgColor = remember { mutableStateOf(Color.Red) }

    val context = LocalContext.current

    // FlashLight
    val isFlashOn = remember { mutableStateOf(false) }
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager // initializing our camera manager.

    val analyzerUseCase = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetResolution(Size(640, 480))
        .build()


    // Image analysis
    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    // Face recognition options
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    analyzerUseCase.setAnalyzer(executor) { imageProxy ->
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val detector = FaceDetection.getClient(options)
            // Pass image to an ML Kit Vision API
            detector.process(image)
                .addOnSuccessListener { faces ->
                    Log.e("ML Kit", "detected ${faces.size} faces")
                    if(faces.size == 1) {
                        if (boxBgColor.value != Color.Green) {
                            boxBgColor.value = Color.Green
                            showProgressBar.value = true
                        }
                    }else {
                        boxBgColor.value = Color.Red
                        showProgressBar.value = false
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ML Kit", "error $e")
                }
            }

        //mediaImage?.close()
        imageProxy.close()
    }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = Preview.Builder().apply {
        setDefaultResolution(Size(640, 480))
        setTargetAspectRatio(AspectRatio.RATIO_4_3)
    }.build()
    val previewView = remember { PreviewView(context) }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()


    LaunchedEffect(lensFacing, cameraZoomRatio.value) {// this will run every time we change the lensFacing
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            analyzerUseCase
        )
        val cameraControl = camera.cameraControl
        cameraControl.setZoomRatio(cameraZoomRatio.value)

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier
                .size(370.dp, 370.dp)
                .clip(RoundedCornerShape(1000.dp))
                .background(boxBgColor.value),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                { previewView },
                modifier = Modifier
                    .width(350.dp)
                    .height(350.dp)
                    .clip(RoundedCornerShape(1000.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (showProgressBar.value) {

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 70.dp)
                    .height(15.dp)
                    .border(1.dp, Color.Black)
                    .clip(RoundedCornerShape(10.dp)),
                color = Color.Green,
            )
        }
        if (!showProgressBar.value) {
            Slider(
                modifier = Modifier.padding(start = 70.dp, end = 70.dp),
                value = cameraZoomRatio.value,
                onValueChange = {
                    cameraZoomRatio.value = it
                },
                valueRange = 1f..3f
            )
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

                FlashlightToggleButton {
                    isFlashOn.value = !isFlashOn.value
                    enableFlashlight(cameraManager, isFlashOn.value)
                }
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