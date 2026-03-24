plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "wang.zengye.dsm"
    compileSdk = 36

    defaultConfig {
        applicationId = "wang.zengye.dsm"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 分架构编译，生成多个 APK
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true // 同时生成包含所有架构的 universal APK
        }
    }

    signingConfigs {
        // 只有在签名文件存在时才创建 release 签名配置
        val storeFilePath = System.getenv("RELEASE_STORE_FILE") ?: findProperty("RELEASE_STORE_FILE") as? String
        if (storeFilePath != null && storeFilePath.isNotEmpty() && file(storeFilePath).exists()) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: findProperty("RELEASE_STORE_PASSWORD") as? String ?: ""
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: findProperty("RELEASE_KEY_PASSWORD") as? String ?: ""
                keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: findProperty("RELEASE_KEY_ALIAS") as? String ?: ""
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // 如果签名配置存在则使用，否则使用默认调试签名
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        // 禁用某些 Compose 相关的 lint 检查
        disable += setOf(
            "LocalContextGetResourceValueCall",
            "LocalContextConfigurationRead"
        )
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
        jniLibs {
            useLegacyPackaging = true
            // 使用 pickFirsts 选择第一个遇到的 libc++_shared.so
            // 由于 MPV 依赖在前，它的版本会被选中
            pickFirsts += setOf(
                "lib/arm64-v8a/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so",
                "lib/x86/libc++_shared.so",
                "lib/x86_64/libc++_shared.so",
                "lib/mips/libc++_shared.so",
                "lib/mips64/libc++_shared.so"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.coil.compose)
    implementation(libs.coil.base)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)

    // DocumentFile for SAF
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Vico 图表库
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // 图片预览和加载
    implementation(libs.coil.gif)

    // Telephoto 可缩放图片查看器
    implementation(libs.telephoto.zoomable.image.coil)
    
    // mpv-android 视频播放器库
    implementation(libs.mpv.android.lib)
    
    // Seeker 进度条库
    implementation(libs.seeker)
    
    // ConstraintLayout Compose
    implementation(libs.androidx.constraintlayout.compose)
    
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // SSH 库
    implementation(libs.sshlib) {
        exclude(group = "com.google.crypto.tink", module = "tink")
    }

    // 终端模拟器库（ConnectBot termlib，支持完整 VT100/xterm 模拟）
    implementation(libs.termlib)
    
    // 二维码生成
    implementation(libs.zxing.core)

    // Moshi JSON 解析
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    implementation(libs.retrofit.moshi)

    // Room 数据库
    implementation("androidx.room:room-runtime:2.7.0-alpha12")
    implementation("androidx.room:room-ktx:2.7.0-alpha12")
    ksp("androidx.room:room-compiler:2.7.0-alpha12")

    // Unit Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    
    // Android Instrumented Test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
