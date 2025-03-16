import com.android.build.api.variant.ResValue
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
	autowire(libs.plugins.android.application)
	autowire(libs.plugins.kotlin.android)
	autowire(libs.plugins.kotlin.ksp)
	autowire(libs.plugins.android.x.room)
}

android {
	signingConfigs {
		getByName("debug") {
			storeFile = file(gradleLocalProperties(rootDir, providers).getProperty("keystore.location"))
			storePassword = gradleLocalProperties(rootDir, providers).getProperty("keystore.password")
			keyPassword = gradleLocalProperties(rootDir, providers).getProperty("keystore.password")
			keyAlias = gradleLocalProperties(rootDir, providers).getProperty("keystore.alias")
		}
		create("release") {
			storeFile = file(gradleLocalProperties(rootDir, providers).getProperty("keystore.location"))
			storePassword = gradleLocalProperties(rootDir, providers).getProperty("keystore.password")
			keyPassword = gradleLocalProperties(rootDir, providers).getProperty("keystore.keypass")
			keyAlias = gradleLocalProperties(rootDir, providers).getProperty("keystore.alias")

		}
	}
	namespace = property.project.app.packageName
	compileSdk = 35

	defaultConfig {
		applicationId = property.project.app.packageName
		minSdk = property.project.android.minSdk
		targetSdk = property.project.android.targetSdk
		versionName = property.project.app.versionName
		versionCode = property.project.app.versionCode
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		externalNativeBuild {
			cmake {
				cppFlags += ""
			}
		}

	}
	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
		debug {
			isMinifyEnabled = false
			isShrinkResources = false
			applicationIdSuffix = ".testtt"
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
		freeCompilerArgs = listOf(
			"-Xno-param-assertions",
			"-Xno-call-assertions",
			"-Xno-receiver-assertions"
		)
	}
	buildFeatures {
		buildConfig = true
		viewBinding = true
		aidl = true
	}
	androidComponents {
		onVariants { variant ->
			variant.resValues.put(
				variant.makeResValueKey("string", "app_id"),
				ResValue(variant.applicationId.get()))
		}
	}
	lint { checkReleaseBuilds = false }
	room {
		schemaDirectory("$projectDir/schemas")
	}
	externalNativeBuild {
		cmake {
			path = file("src/main/cpp/CMakeLists.txt")
			version = "3.22.1"
		}
	}
	// TODO Please visit https://highcapable.github.io/YukiHookAPI/en/api/special-features/host-inject
	// TODO 请参考 https://highcapable.github.io/YukiHookAPI/zh-cn/api/special-features/host-inject
	// androidResources.additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x64")
}

dependencies {
	implementation("androidx.legacy:legacy-support-v4:1.0.0")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
	implementation("androidx.fragment:fragment-ktx:1.8.6")
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("androidx.preference:preference-ktx:1.2.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	implementation("androidx.constraintlayout:constraintlayout:2.2.1")
	implementation("androidx.activity:activity-ktx:1.10.1")

	// ~ <room> ~
	val roomVersion = "2.7.0-rc02"
	implementation("androidx.room:room-runtime:$roomVersion")
	ksp("androidx.room:room-compiler:$roomVersion")
	implementation("androidx.room:room-ktx:$roomVersion")
	// ~ </room> ~


	// ~ <magisk> ~
	val libsuVersion = "6.0.0"
	implementation("com.github.topjohnwu.libsu:core:${libsuVersion}")
	implementation("com.github.topjohnwu.libsu:service:${libsuVersion}")
	implementation("com.github.topjohnwu.libsu:nio:${libsuVersion}")
	// ~ </magisk> ~

	implementation("io.arrow-kt:arrow-core:2.0.1")
	compileOnly(de.robv.android.xposed.api)
	implementation(com.highcapable.yukihookapi.api)
	implementation("com.google.android.material:material:1.12.0")
	ksp(com.highcapable.yukihookapi.ksp.xposed)
	implementation(com.github.duanhong169.drawabletoolbox)
	implementation(androidx.core.core.ktx)
	implementation(androidx.appcompat.appcompat)
	implementation(com.google.android.material.material)
	implementation(androidx.constraintlayout.constraintlayout)
	testImplementation(junit.junit)
	androidTestImplementation(androidx.test.ext.junit)
	androidTestImplementation(androidx.test.espresso.espresso.core)
}