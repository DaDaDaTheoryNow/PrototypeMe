rootProject.name = "PrototypeMe"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// :composeApp folder is the Android/Desktop/Web application entry point, mapped as :app
include(":app")
project(":app").projectDir = file("composeApp")

// Library modules
include(":core:mvi")
include(":core:ui")
include(":domains:board:api:data")
include(":domains:board:api:domain")
include(":domains:board:impl:data")
include(":domains:board:impl:domain")
include(":feature:board")

