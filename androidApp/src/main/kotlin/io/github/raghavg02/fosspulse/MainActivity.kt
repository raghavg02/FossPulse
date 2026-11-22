package io.github.raghavg02.fosspulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.raghavg02.fosspulse.app.di.initKoin
import io.github.raghavg02.fosspulse.data.local.appContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        appContext = applicationContext

        initKoin()

        setContent {
            App()
        }
    }
}
