import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    id("app.cash.sqldelight") version "2.1.0"
}

sqldelight {
    databases {
        create("WatsonDatabase") {
            packageName.set("com.watson.database")
        }
    }
    linkSqlite = true
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared2"
            isStatic = false
            export("io.insert.koin:koin-core")
        }
    }

    jvm()

    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {

            // Koin 核心库
            implementation(libs.koin.core)
            // Koin for Compose Multiplatform (提供 koinInject() 等)
            implementation(libs.koin.compose)
            // Ktor 核心客户端
            implementation(libs.ktor.client.core)

            // 1. Ktor 引擎 (Engine) - 这是 Ktor 的核心概念！
            // Ktor 需要为每个平台指定一个具体的 HTTP 请求实现。
            // 你需要为你支持的每个平台都添加一个引擎。
            // Ktor 会在编译时自动选择正确的引擎。


            // 为 JVM/Desktop 添加 CIO 引擎 (也可以用 OkHttp)
            implementation(libs.ktor.client.cio)

            // 2. Ktor 功能插件 (Plugins) - 按需添加
            // 内容协商插件，用于自动处理 JSON
            implementation(libs.ktor.client.content.negotiation)
            // 使用 kotlinx.serialization 进行 JSON 解析
            implementation(libs.ktor.serialization.kotlinx.json)

            // 日志插件，方便调试
            implementation(libs.ktor.client.logging)

            // SQLDelight 运行时库
            implementation(libs.runtime)
            // Coroutines 扩展，强烈推荐，用于 Flow 支持
            implementation(libs.coroutines.extensions)
        }

        jvmMain.dependencies {
            // JVM/Desktop 平台的数据库驱动
            implementation(libs.sqlite.driver)
        }
        androidMain.dependencies {
            // 为 Android 添加 OkHttp 引擎
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.android.driver)
            // Koin Android specific helpers (e.g., for androidContext())
            implementation(libs.koin.android)
        }
        appleMain.dependencies {
            // put your Multiplatform dependencies here
            implementation(libs.ktor.client.darwin)

        }
        nativeMain.dependencies {
            // iOS/Native 平台的数据库驱动
            implementation(libs.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
