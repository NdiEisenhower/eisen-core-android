plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.eisen.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.eisen.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-webview"))
    implementation(project(":core-billing"))
    implementation(project(":core-auth"))
    implementation(project(":core-updater"))
    implementation(project(":core-ads"))
    implementation(project(":core-review"))
    implementation(project(":core-notifications"))
    implementation(project(":core-analytics"))
    implementation(project(":core-games"))
    implementation(project(":core-share"))
    implementation(project(":core-security"))
    implementation(project(":core-permissions"))
    implementation(project(":core-turnkey"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}
