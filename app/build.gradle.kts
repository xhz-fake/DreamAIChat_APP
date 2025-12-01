plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.dreamaichat_app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dreamaichat_app"
        minSdk = 21
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
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        viewBinding = true
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // 基础UI组件
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.fragment)
    
    // 网络层
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.rxjava3)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // 异步处理
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    
    // 数据持久化
    implementation(libs.room.runtime)
    implementation(libs.room.rxjava3)
    kapt(libs.room.compiler)
    
    // 架构组件
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    
    // 导航组件
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // JSON 解析
    implementation(libs.gson)
    implementation(libs.glide)
    
    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
