pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven(url = "https://developer.huawei.com/repo/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://developer.huawei.com/repo/")
    }
}

rootProject.name = "eisen-core-android"

include(":core-common")
include(":core-webview")
include(":core-billing")
include(":core-auth")
include(":core-updater")
include(":core-ads")
include(":core-review")
include(":core-notifications")
include(":core-analytics")
include(":core-games")
include(":core-share")
include(":core-security")
include(":core-permissions")
include(":sample-app")
