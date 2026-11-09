plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.eisen.core.ads"
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
    implementation(libs.play.services.ads)
    implementation(libs.huawei.ads)
    implementation(libs.kotlinx.coroutines.android)
}
