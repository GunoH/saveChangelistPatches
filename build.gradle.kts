buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.30")
    }
}

plugins {
    kotlin("jvm") version "1.2.30"
    id("org.jetbrains.intellij") version "0.3.1"
}

intellij {
    pluginName = "SaveChangeListPatches"
    version = "IC-2016.1"
    updateSinceUntilBuild = false
}

group = "nl.guno"
version = "1.5.0pre"
