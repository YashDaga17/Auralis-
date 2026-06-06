import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val envProperties = Properties().apply {
    val file = rootProject.file(".env")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun secretOrEmpty(name: String): String =
    localProperties.getProperty(name)
        ?: envProperties.getProperty(name)
        ?: providers.environmentVariable(name).orNull
        ?: ""

fun configOrDefault(name: String, defaultValue: String): String =
    localProperties.getProperty(name)
        ?: envProperties.getProperty(name)
        ?: providers.environmentVariable(name).orNull
        ?: defaultValue

fun buildConfigString(value: String): String =
    "\"" + value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"") + "\""

android {
    namespace = "com.brandforge.app"
    compileSdkVersion("android-36.1")

    defaultConfig {
        applicationId = "com.brandforge.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPENROUTER_API_KEY", buildConfigString(secretOrEmpty("OPENROUTER_API_KEY")))
        buildConfigField("String", "GEMINI_API_KEY", buildConfigString(secretOrEmpty("GEMINI_API_KEY")))
        buildConfigField("String", "FIRECRAWL_API_KEY", buildConfigString(secretOrEmpty("FIRECRAWL_API_KEY")))
        buildConfigField("String", "APIFY_API_TOKEN", buildConfigString(secretOrEmpty("APIFY_API_TOKEN")))
        buildConfigField("String", "QDRANT_URL", buildConfigString(secretOrEmpty("QDRANT_URL")))
        buildConfigField("String", "QDRANT_API_KEY", buildConfigString(secretOrEmpty("QDRANT_API_KEY")))
        buildConfigField("String", "YOUTUBE_API_KEY", buildConfigString(secretOrEmpty("YOUTUBE_API_KEY")))
        buildConfigField("String", "OPENROUTER_BASE_URL", buildConfigString(configOrDefault("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1/")))
        buildConfigField("String", "GEMINI_BASE_URL", buildConfigString(configOrDefault("GEMINI_BASE_URL", "https://generativelanguage.googleapis.com/")))
        buildConfigField("String", "FIRECRAWL_BASE_URL", buildConfigString(configOrDefault("FIRECRAWL_BASE_URL", "https://api.firecrawl.dev/")))
        buildConfigField("String", "APIFY_BASE_URL", buildConfigString(configOrDefault("APIFY_BASE_URL", "https://api.apify.com/v2/")))
        buildConfigField("String", "YOUTUBE_BASE_URL", buildConfigString(configOrDefault("YOUTUBE_BASE_URL", "https://www.googleapis.com/youtube/v3/")))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lottie.compose)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
