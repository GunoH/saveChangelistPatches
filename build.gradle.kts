buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${BuildConfig.kotlinVersion}")
    }
}

plugins {
    kotlin("jvm") version BuildConfig.kotlinVersion
    id("org.jetbrains.intellij") version BuildConfig.gradleIntellijPluginVersion 
}

intellij {
    pluginName = "SaveChangeListPatches"
    version = BuildConfig.intellijVersion
    updateSinceUntilBuild = false
}

group = "nl.guno"
version = BuildConfig.pluginVersion
