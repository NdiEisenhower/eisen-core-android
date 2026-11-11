plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.eisen.core.games"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Google Play Games Services v2
    implementation(libs.play.services.games.v2)

    // Huawei Game Service
    implementation(libs.huawei.game)
}

