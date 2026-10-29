plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("io.sentry.android.gradle") version "6.22.0"
}

android {
    namespace = "com.kitlabs.aiusagedemo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kitlabs.aiusagedemo"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TOKEN", "\"eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwidHlwIjoiSldUIiwiY3R5IjoiSldUIn0.o7qYXMP6fM1Dvx8uMnf-l-hlNnGsBFDEB9mw1dKZHtlTq0U0XuZI7pW4rglbYzd2vgZXc7qlGANf8QVSHoY_Mz9xBv6kRyAM.ZUNIFUPAp-kBHj-UYBKcSQ.KSbpVTcgVYuIrrNcLLBus1dGbIIP0NKVtMB1gyXA1_ImMfwN3yKfdOGbKD_F_eJ2_0ppHLR-CvO4VaxD5By2Ox8hw5MQH-nflBorFZstTqzDkb9Q2umVPBOGfQvsUDrB8dLUKNs9R7v3VmbE6jMI30Ljj76_2GxQTHzmGW9ZLgpXF74LmMDJtaiISwMMM9ha2VOZC3mxSkeN4Yk3DDbZfZPdHrkdEZyE0fDSDLQ_GBORj7jU5qsv7pk4a-_rYcHnj3rJDljOMH8ot4txAAj0-2jl4t3Ya9b6K9TQfAASuQ9ApVtkC9AkwtwzcmAevc_Q6QeeBY7SZux2rQwNObTNKeMH-yKHTvK8lSVCHpUCeKE7X_OgUAc-TlgVgsyFPl8EC0eeXEalpqFfXxCjh92yciQTu4-Ykd1Rv7eVC44dth2ZES5Lvu414o6TNJhR_2O8AaY-NF4Ve9WB3z2Ad4Mp4V7tyOn7NjMH_6BH1PAvt3TJne3PG4-uZgTINs5Ze27txJ_bOT2Azt--9ZpKBDi_Vha8zDpErYZM3DNhxdI6fdJkidp_P0_HSXCW4RFuhWl8EJBp2rZ9o99JxwN0oZ9M5DholOr6RPHWGuXPHWVfHXvyQP1yZlwxBw4MKodRnzAuX4ztBWQt6UTJmaNhUaq-lHQpONyBfqO3hOFIqhRVUrnqYSeQcGh_J5C-fSLFDjn-VR7qfPoaO3hlKygwb2L_9aTfC_TTB6puxCteFkwOHqqXpWE-cjsrwV-PT9OSxSvNyVeK8q2KBW1S3xzH88yjyUy7_geCHXAK7Kwmc1xx0h4EDbsBCSei2q6VcBRMR2jR0JZLnK0vQTdOA7fR7QUIollGuhT9ssBODAsUhsYu6NgiLbDAVMom0JYMo_BQfqeAaZyv2iYZHKlglTkETTbKaRJU4kOFK3RRUDGztnXafBM4-Ov6sGRIZ16C8jBXeVLhGzXZmE0SD5W_V3KhehpLKOOzew8FJ0CfX8H8iIrpuVaPICrcXPjayRLyfC-Vobna1QH1ibfOTEDmV5b-17kyXmvOMTlwaj6hR-C6jkNpBPpf2q2ZEKKiO-xV4mGvrzrnw8Kdji32rtM6LnR1qhG-4WbzbzivlVPFsbhZUYZSb5mW7eOag9jAqnxWjkzEcJHnony_byJz9iXjAXyE3w3hlpV2LglCS-qIMY3LkLOvpioGmodINSfHuPX0CiPbIkjIMYYUYMaXiCG7nT5UxciCY2oNTx-cIo4Cr6rwUTRJCEsfOOoIYpvkCY9hUqUp4dXOZswAQPJIY0L_mhIzpyEj5DTupu2TATkYxqvmshc092qtbgKhvltF86Y6HmtA86hw.M5YOv1ssPTZ0j0cj6fpgABKq9O5NPnFE2NUIoMUcWKs\"")
    }

    buildFeatures {
        buildConfig = true
    }

//    flavorDimensions += "environment"
//
//    productFlavors {
//        create("dev") {
//            dimension = "environment"
//            applicationIdSuffix = ".dev"
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.coroutines.android)

    // ViewModel + Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("io.sentry:sentry-android:8.57.0")

    implementation("com.posthog:posthog-android:3.+")

    implementation("me.pushy:sdk:1.0.127")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}