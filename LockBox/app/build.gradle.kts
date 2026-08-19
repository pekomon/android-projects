plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pekomon.lockbox"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.pekomon.lockbox"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("checkConnectedAndroidUserUnlocked") {
    group = "verification"
    description = "Fails connected tests early when the selected Android device user is still credential-locked."

    doLast {
        val sdkRoot = providers
            .environmentVariable("ANDROID_HOME")
            .orElse(providers.environmentVariable("ANDROID_SDK_ROOT"))
            .orElse("${System.getProperty("user.home")}/Library/Android/sdk")
            .get()
        val adbExecutable = file("$sdkRoot/platform-tools/adb")
        val adbCommand = if (adbExecutable.exists()) adbExecutable.absolutePath else "adb"
        val requestedSerial = providers.environmentVariable("ANDROID_SERIAL").orNull

        fun runAdb(vararg args: String): String {
            val process = ProcessBuilder(adbCommand, *args)
                .redirectErrorStream(false)
                .start()
            val output = process.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            val error = process.errorStream.use { it.readBytes().toString(Charsets.UTF_8) }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw GradleException(
                    "Unable to run adb ${args.joinToString(" ")}.\n" +
                        error.trim(),
                )
            }
            return output
        }

        val devices = runAdb("devices")
            .lineSequence()
            .map { it.trim() }
            .filter { it.endsWith("\tdevice") }
            .map { it.substringBefore('\t') }
            .toList()
        val serial = requestedSerial ?: devices.singleOrNull()
            ?: throw GradleException(
                "Connected tests require exactly one unlocked Android device/emulator, or ANDROID_SERIAL must be set.\n" +
                    "Connected devices: ${devices.ifEmpty { listOf("none") }.joinToString()}",
            )

        val userDump = runAdb("-s", serial, "shell", "dumpsys", "user")
        val userState = Regex("""State: (\S+)""")
            .find(userDump)
            ?.groupValues
            ?.get(1)
            ?: "unknown"

        if (userState != "RUNNING_UNLOCKED") {
            throw GradleException(
                "Connected tests require an unlocked Android user on $serial.\n" +
                    "Current state: $userState\n" +
                    "Unlock the emulator/device first. For the local AVD, wake it and enter PIN 1234 when configured.",
            )
        }
    }
}

tasks.matching { it.name == "connectedDebugAndroidTest" }.configureEach {
    dependsOn("checkConnectedAndroidUserUnlocked")
}
