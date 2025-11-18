// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}

/**
 * Android Studio crea tasks "ijDownloadArtifact..." para bajar sources/javadoc.
 * Google no publica play-services-tasks-*-sources.jar, así que forzamos a deshabilitarlos
 * en TODOS los módulos (root y :app).
 */
allprojects {
    tasks.configureEach {
        if (name.startsWith("ijDownloadArtifact")) {
            enabled = false
        }
    }
}
