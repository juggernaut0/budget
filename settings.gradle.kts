pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://juggernaut0.github.io/m2/repository")
    }
}

plugins {
    id("dev.twarner.settings") version "1.0.6"
}

include("api", "dbmigrate", "service", "web")
rootProject.name = "budget"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
