import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService

@Composable
fun torchApplication(context: Context) {
    val torchStatus = remember {
        mutableStateOf(false)
    }
    val torchMsg = remember {
        mutableStateOf("Off")
    }
    // on below line we are creating a column,
    Column(
        // on below line we are adding
        // a modifier to it,
        modifier = Modifier
            .fillMaxSize()
            // on below line we are adding a padding.
            .padding(all = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // on below line we are creating a text
        // for displaying torch status.
        Text(
            text = "Torch is " + torchMsg.value,
            // on below line we are
            // displaying a text color
            color = Color.Black,

            // on below line we are
            // setting font weight
            fontWeight = FontWeight.Bold,

            // on below line we are setting
            // font family
            fontFamily = FontFamily.Default,

            // on below line we are setting
            // font size and padding.
            fontSize = 20.sp, modifier = Modifier.padding(5.dp)
        )

        // on below line creating a switch for displaying a torch
        Switch(checked = torchStatus.value, onCheckedChange = {
            torchStatus.value = it


            // creating a string for camera ID
            lateinit var cameraID: String

            // initializing our camera manager.
            var cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                // O means back camera unit, 1 means front camera unit
                // on below line we are getting camera id for back camera as we will be using torch for back camera
                cameraID = cameraManager.cameraIdList[0]
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                if(torchStatus.value) {
                    cameraManager.setTorchMode(cameraID, true)
                    Toast.makeText(context, "Torch turned on", Toast.LENGTH_LONG).show()
                } else {
                    cameraManager.setTorchMode(cameraID, false)
                    Toast.makeText(context, "Torch turned off", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}
