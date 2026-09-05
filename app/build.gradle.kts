plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.smartledger.aldaftar"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.smartledger.aldaftar"
    minSdk = 24
    targetSdk = 36
    versionCode = 4
    versionName = "1.3"

    // بصمة شهادة الإصدار الإنتاجي تُمرر من إعدادات البناء أو متغيرات البيئة ولا تُخمن داخل الشيفرة.
    val expectedReleaseCert = providers.gradleProperty("RELEASE_CERT_SHA256").orNull ?: System.getenv("RELEASE_CERT_SHA256") ?: ""
    buildConfigField("String", "EXPECTED_RELEASE_CERT_SHA256", "\"${expectedReleaseCert}\"")
    val integrityProject = providers.gradleProperty("PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER").orNull ?: System.getenv("PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER") ?: "0"
    buildConfigField("long", "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", integrityProject)

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  androidResources {
    localeFilters += listOf("ar", "en")
  }

  signingConfigs {
    create("release") {
      val configuredKeystorePath =
        providers.gradleProperty("RELEASE_STORE_FILE").orNull ?: "aldaftar.keystore"
      // يُحل مسار ملف التوقيع من مجلد وحدة التطبيق.
      // يُقبل المسار المحلي المباشر ومسار بيئة التكامل المستمر بعد تنظيف السابقة.
      // يمنع التنظيف تكوين مسار مكرر قد يؤدي إلى ملف توقيع غير صحيح.
      val keystorePath = configuredKeystorePath
        .removePrefix("app/")
        .removePrefix("./")
        .ifBlank { "aldaftar.keystore" }
      val storePwd = providers.gradleProperty("RELEASE_STORE_PASSWORD").orNull
      val keyAli = providers.gradleProperty("RELEASE_KEY_ALIAS").orNull ?: "aldaftar"
      val keyPwd = providers.gradleProperty("RELEASE_KEY_PASSWORD").orNull

      if (storePwd != null && keyAli != null && keyPwd != null) {
        storeFile = file(keystorePath)
        storePassword = storePwd
        keyAlias = keyAli
        keyPassword = keyPwd
        enableV1Signing = true
        enableV2Signing = true
        enableV3Signing = true
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val relConfig = signingConfigs.getByName("release")
      if (relConfig.storePassword != null) {
        signingConfig = relConfig
      }
    }
    debug {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "/META-INF/INDEX.LIST"
      excludes += "/META-INF/DEPENDENCIES"
      excludes += "/META-INF/*.version"
      excludes += "/META-INF/*.kotlin_module"
      excludes += "/META-INF/licenses/**"
      excludes += "**/proto/**"
    }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// يُضبط مكون الأسرار لاستخدام ملف الإعداد المحلي والملف النموذجي.
// الغرض هو توحيد مصدر إعدادات الأسرار بين بيئات البناء مع إبقاء القيم خارج الشيفرة المصدرية.
val envFile = rootProject.file(".env")
if (!envFile.exists()) {
  val googleClientId = System.getenv("GOOGLE_CLIENT_ID") ?: ""
  val googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET") ?: ""
  val geminiApiKey = System.getenv("GEMINI_API_KEY") ?: ""
  if (googleClientId.isNotEmpty() || googleClientSecret.isNotEmpty() || geminiApiKey.isNotEmpty()) {
    envFile.writeText("""
      GOOGLE_CLIENT_ID=$googleClientId
      GOOGLE_CLIENT_SECRET=$googleClientSecret
      GEMINI_API_KEY=$geminiApiKey
    """.trimIndent())
  }
}

// تُنقح أسطر ملف الأسرار لمنع القيم الفارغة التي قد تنتج إعدادات بناء غير صالحة.
// لا تُستخدم هذه المعالجة لإجراء أي حساب مالي أو تشغيل عمل ثقيل على خيط الواجهة.
if (envFile.exists()) {
  try {
    val lines = envFile.readLines().map { line ->
      if (line.contains("=")) {
        val parts = line.split("=", limit = 2)
        val key = parts[0].trim()
        val value = parts[1].trim()
        if (value.isEmpty()) {
          "$key=none"
        } else {
          line
        }
      } else {
        line
      }
    }
    envFile.writeText(lines.joinToString("\n"))
  } catch (e: Exception) {
    // تُمنع مشكلة ملف الإعداد من إسقاط عملية البناء بسبب خطأ جانبي في التنقيح.
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
  implementation(platform(libs.androidx.compose.bom))
  implementation("androidx.profileinstaller:profileinstaller:1.3.1")
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation("androidx.compose.ui:ui-text-google-fonts:1.6.3")
  implementation(libs.androidx.core.ktx)
  implementation("androidx.core:core-splashscreen:1.0.1")
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.okhttp)
  implementation(libs.play.services.auth)
  implementation(libs.play.integrity)
  implementation(libs.androidx.security.crypto)
  implementation(libs.androidx.biometric)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.paging.runtime)
  implementation(libs.androidx.paging.compose)
  implementation(libs.androidx.room.paging)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}




