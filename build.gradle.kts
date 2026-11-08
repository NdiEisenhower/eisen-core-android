plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    `maven-publish`
}

subprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("release") {
                    from(components.findByName("release") ?: return@create)
                    groupId = "com.github.NdiEisenhower.eisen-core-android"
                    artifactId = project.name
                    version = "1.0.0"
                }
            }
        }
    }
}
