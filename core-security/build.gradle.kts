plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.eisen.core.security"
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
    implementation(libs.androidx.appcompat)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.biometric.ktx)
    api(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.android)
}

