plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.gms.google-services")
  id("org.jetbrains.kotlin.kapt")
  id("com.google.protobuf")
}

android {
  namespace = "com.repinfaust.mandrake"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.repinfaust.mandrake"
    minSdk = 24
    targetSdk = 34
    versionCode = 2
    versionName = "1.3"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures { compose = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin { jvmToolchain(17) }
  kotlinOptions { jvmTarget = "17" }

  packaging {
    resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
  }
}

dependencies {
  // --- Room ---
  val room = "2.6.1"
  implementation("androidx.room:room-runtime:$room")
  implementation("androidx.room:room-ktx:$room")
  kapt("androidx.room:room-compiler:$room")

  // --- DataStore (Proto runtime) ---
  implementation("androidx.datastore:datastore:1.1.1")
  implementation("com.google.protobuf:protobuf-javalite:3.25.3")

  // --- Navigation-Compose ---
  implementation("androidx.navigation:navigation-compose:2.8.2")

  // --- Firebase ---
  implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.firebase:firebase-firestore-ktx")

  // Google Identity
  implementation("com.google.android.gms:play-services-auth:21.2.0")

  // Tasks.await()
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

  // (Optional if you still use Preferences elsewhere)
  implementation("androidx.datastore:datastore-preferences:1.1.1")

  // --- JSON serialization ---
  implementation("com.google.code.gson:gson:2.10.1")
  
  // --- Google Play Billing ---
  implementation("com.android.billingclient:billing-ktx:6.1.0")
  implementation("com.google.firebase:firebase-functions-ktx")

  // --- AndroidX / Compose ---
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.core:core-splashscreen:1.0.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation(platform("androidx.compose:compose-bom:2024.10.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("com.google.android.material:material:1.12.0")

  // --- Debug / Test ---
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

// ✅ Protobuf codegen config MUST be at top level (not inside dependencies)
protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.25.3"
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        create("java") { option("lite") }
      }
    }
  }
}
