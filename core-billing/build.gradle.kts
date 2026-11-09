plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.eisen.core.billing"
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
    implementation(libs.google.play.billing)
    implementation(libs.huawei.iap)
    implementation(libs.kotlinx.coroutines.android)
}
