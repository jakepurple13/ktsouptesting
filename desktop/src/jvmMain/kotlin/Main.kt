import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.common.AndroidXDataStore
import com.example.common.UIShow

fun main() = application {
    LaunchedEffect(Unit) {
        AndroidXDataStore.updateLastUpdate(System.currentTimeMillis())
    }
    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            UIShow()
        }
    }
}
