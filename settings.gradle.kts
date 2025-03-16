pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
				maven { url = uri("https://jitpack.io") }
    }
}
plugins {
    id("com.highcapable.sweetdependency") version "1.0.4"
    id("com.highcapable.sweetproperty") version "1.0.5"
}
sweetProperty {
    rootProject { all { isEnable = false } }
}

rootProject.name = "bg_clipboard"
include(":app")