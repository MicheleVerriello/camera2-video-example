import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material.icons.outlined.FlipCameraIos
import androidx.compose.material.icons.outlined.Light
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraView() {

    val context = LocalContext.current
    var lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview = Preview.Builder().build()
    var previewView = remember { PreviewView(context) }
    var cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Column {
        AndroidView(
            { previewView },
            modifier = Modifier
                .width(380.dp)
                .height(380.dp)
                .clip(RoundedCornerShape(500.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
        ){
            IconButton(onClick = {

                lensFacing = if(lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }

                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                preview = Preview.Builder().build()

            }) {
                Icon(imageVector = Icons.Outlined.FlipCameraAndroid, contentDescription = "Flip camera")
            }

            IconButton(onClick = {
                onOffTorch()
            }) {
                Icon(imageVector = Icons.Outlined.Light, contentDescription = "Torch")
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

private fun onOffTorch() {

}