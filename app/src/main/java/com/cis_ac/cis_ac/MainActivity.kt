package com.cis_ac.cis_ac

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.cis_ac.cis_ac.data.notification.ChatStateManager
import com.cis_ac.cis_ac.data.notification.FirestoreNotificationRepository
import com.cis_ac.cis_ac.data.notification.NotificationContextHolder
import com.cis_ac.cis_ac.data.notification.NotificationInitializer
import com.cis_ac.cis_ac.ui.navigation.AppNav
import com.cis_ac.cis_ac.ui.theme.CISACTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            initializeNotifications()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash
        installSplashScreen().apply { setKeepOnScreenCondition { false } }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Context para notificaciones
        NotificationContextHolder.initialize(this)

        // Permisos notificaciones
        requestNotificationPermission()

        setContent {
            CISACTheme {
                AppNav()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ChatStateManager.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        ChatStateManager.setAppForeground(false)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    initializeNotifications()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12-: no se requiere permiso
            initializeNotifications()
        }
    }

    private fun initializeNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authRepository = FirebaseAuthRepository()
                val notificationRepository = FirestoreNotificationRepository()
                val initializer = NotificationInitializer(
                    authRepository = authRepository,
                    notificationRepository = notificationRepository
                )
                initializer.initializeNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
