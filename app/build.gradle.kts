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
        buildConfigField("String", "TOKEN", "\"eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwidHlwIjoiSldUIiwiY3R5IjoiSldUIn0.ZOceR8b-xbBX_3mKgHhBtL8MBJESDcBDn0CpUNRisQ46pW4QMoi_eJzSTcJHE9dyEeE2V-JtHt_RAO0FjUIR0I-6XuTEsft1.GQk4-ZY7bBbuespEMFO11w.0qa6iaMxYmX-xcu5_yOHSIAnu_7q79esWnjqvvUG2sDqOSsz6SNoRLSjR-bLHG5mV5Eq1WO7ZhcA5kSu4i0vWebuCzDjV8rsxhE4cvq1MSNVslyMeKO9XUx_GKSIo2oG2CkQowM1q9CYztDc28nhdEM-ezA0afeSEvtcmG054ybKYjCniRSdhkDmx0aULN5SHacvsB7rB82OKwoZCyNHTVRcYFYEBj1uxBtQkOYk8w2JK6pZL1tlr7YewXZS_xBwADLk-X7ql6kChtjlf_7jaoEBOoLmLarp3771SuC1-LKvZ9-KcqPhxpuQ3EgohH7baIqgU1Zh2TG3SU3PW-gmvmUVbV1kM9_q6Cm3lezmhEhIgOgIh1I3XMqZQJeKWt7wzxU9If-d5LR3Za5S-LxR5yI96nrA8ZZqRXaGwiHaMss3iKBajX0LeO0tCwMFX_gsrI98J0nAr53NlvOXN6ItvtorU52vwW1cbWlzBsSRArZP84YqzEhRw-M1IKIEtIoVU3bPpJ78pEjsrxrINT-yQYse6uyjN-pxn5TjUc4oerW7GwLtt5-SZ5asm188P2MBDDM0yaxxPZFSCl_BqOZEBgMhFMnS5In74qnIcIfY8z4afat7mhvZ_UIW-kFJOHDHd6Z-pE3qWTY0-OpMf_z4nA8iATBBdO_a4cP_f_e2U5NrHXJOUHtd7F3_SaOtw0jH5QrF10eYDoLu1bzp7eryrs3T2hAYm9mN0VraGtx-qPSiC3oMtEQopCiOtHtYqblUW6IEDIp8MZ9yjvwzMIWlrpMECvJn0qPwRUROHATNG-4afeD9M9Nd5-3jzqAvtqMk4SMUrXF8GUBozocgVbFOXHVeYH10mJoZLunvFhiwCFLm00Y-xazCSG4MqGmwgyB9Aq6LQ2hv68GtCFQQDQc90RKNVdLJOWipmAM-RVt-vjJQCah0Egifs6xQfrNcJKt5fT2Lt10lzlbxjGT6tHfg3bOGb2x7mGgjfRAyO4oyYwjW2gSBu1CrsLaxBUMCoC0fmto81LFqYuXmhxjXXKEjjA48DuDGn4D4k3gGNU36ohjikZiRX81qoY9O_18hpY8nJrqDA95ktjTs-_7Dng82eewo6WSZ3hYN7_oi-sN695TR7wEVprgCS11VEltD_wSctav69fBcmiXnwTKY4E59g5Mnav5B2dyofix5UNuU0W13GZu2Z1Oze5mDxm5tcRN0hlcJUn18uQjJCJQWoIlljbs7uUZOtV5DXXi72z5H4XU6gapi_969cdK6_pjwWr9_8d_lv1ewxnsyqeAevaKxV5lzC6jlPyihbzaAs2SjxPIQVj9n5uBHlIGQ1cw6fERW.cDFrLgyQJxFCxA6sc5DmcU2JO6yKnFlVeHaHHEwT0_M\"")
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