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

// Core modules
include(":core:mvi")
include(":core:ui")

// Board Core modules
include(":domains:board-core:api:domain")
include(":domains:board-core:api:data")
include(":domains:board-core:impl:data")

// ERD Design modules
include(":domains:erd-design:api:domain")
include(":domains:erd-design:api:data")
include(":domains:erd-design:impl:data")

// Board System Design modules

// Feature modules
include(":feature:erd-board")
include(":feature:board-core")
