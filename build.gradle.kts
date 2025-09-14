plugins {
  // Android Gradle Plugin (AGP) — stable + widely compatible
  id("com.android.application") version "8.13.0" apply false
  // Kotlin
  id("org.jetbrains.kotlin.android") version "2.0.20" apply false
  // Required with Kotlin 2.0+ for Compose
  id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
  // Add others here only if you use them, e.g. protobuf
  id("com.google.protobuf") version "0.9.4" apply false

  id("com.google.gms.google-services") version "4.4.2" apply false
}
