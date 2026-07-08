import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  
}

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    load(FileInputStream(localPropertiesFile))
  }
}

android {
  namespace = "com.example.pamelapp"
  compileSdk {
    version = release(36) {
      minorApiLevel = 1
    }
  }
  
  defaultConfig {
    buildConfigField(
      "String",
      "BACKEND_BASE_URL",
      "\"https://memoriasbackend-production.up.railway.app\""
    )
    applicationId = "com.example.pamelapp"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "TMDB_TOKEN", "\"${localProperties.getProperty("tmdb_token")}\"")
  }
  
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.foundation.layout)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.savedstate)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging.interceptor)
  implementation(libs.play.services.auth)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.kotlinx.coroutines.core.v173)
  implementation(libs.kotlinx.coroutines.android.v1110)
  
  implementation(platform(libs.firebase.bom))
  
  androidTestImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.androidx.compose.ui.tooling)
  
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.coil.compose)
  implementation(libs.coil.network.okhttp)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  
  // Ktor
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.okhttp)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.client.logging)
  
  implementation(platform(libs.firebase.bom))
  implementation(platform(libs.firebase.bom.v34120))
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services.auth)
  implementation(libs.googleid)
  
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)
  implementation(libs.gson)
  
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}