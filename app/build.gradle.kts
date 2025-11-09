plugins {
    // ۱. تعریف پلاگین‌های اصلی
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // KSP برای Room و بقیه
    // ❌ alias(libs.plugins.kotlin.compose) حذف شد، زیرا باعث خطا می‌شود
}

android {
    // ۲. بلوک android فقط باید یک بار وجود داشته باشد
    namespace = "com.example.mydictionary"
    compileSdk = 36 // یا آخرین نسخه SDK موجود

    defaultConfig {
        applicationId = "com.example.mydictionary"
        minSdk = 24
        targetSdk = 36 // یا آخرین نسخه SDK موجود
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // فعال‌سازی Compose Compiler به روش صحیح
        compose = true
    }
    composeOptions {
        // ✅ فعال‌سازی کامپایلر Compose سازگار با Kotlin 1.9.22 (نسخه 1.5.8)
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    // ۳. وابستگی‌های تمیز شده

    // Compose BOM (باید همیشه اولین مورد باشد)
    implementation(platform(libs.androidx.compose.bom))

    // کتابخانه‌های هسته و Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)

    // ناوبری Compose
    implementation(libs.androidx.navigation.compose)

    // Room - استفاده از Version Catalogs برای خوانایی و مدیریت بهتر
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // برای پشتیبانی از Kotlin Coroutines
    ksp(libs.androidx.room.compiler)

    // تست
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
