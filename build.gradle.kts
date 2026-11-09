plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    `maven-publish`
}

subprojects {
    plugins.withId("com.android.library") {
        apply(plugin = "maven-publish")

        afterEvaluate {
            extensions.configure<PublishingExtension> {
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
}
