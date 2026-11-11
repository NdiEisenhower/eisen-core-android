plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.eisen.core.turnkey"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":core-common"))
    api(project(":core-webview"))
    api(project(":core-billing"))
    api(project(":core-auth"))
    api(project(":core-ads"))
    api(project(":core-games"))
    api(project(":core-notifications"))
    api(project(":core-analytics"))
    api(project(":core-review"))
    api(project(":core-security"))
    api(project(":core-share"))
    api(project(":core-permissions"))
    api(project(":core-updater"))

    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.activity.compose)
    api(libs.androidx.fragment.ktx)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
}
