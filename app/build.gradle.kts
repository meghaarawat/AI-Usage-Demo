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
        buildConfigField("String", "TOKEN", "\"eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwidHlwIjoiSldUIiwiY3R5IjoiSldUIn0.J1-9Rgz93pJZg92fcsDlWv-EmVX7oQ_jIVzRHVSV-zJ80YdP0urWpH4FFuwOlmC730UBsvtyxojQaWYvQim8DF0wmOb0dSzj.mlF5SIwYzJjzUZaNCaBYLQ.us9Cfx_dGudzbhz0-gqqyr689haiWDPjkVpJ_aLBYdy20O3Ur61DcJh0q5NdXQSVZIA2Kslq4XBMxX3peSiivrEi2tcvByxVRQdXks2egguxrgBsTyevpjyjXaQbT14uuAyEq2MEQaFAL_ZaxWyUDmNRqXIOnoUgl2RsyVqWzxxIcFgQR3P8K22JJ0OQvRpuzNyATNgBSE_DE9uzAc7Hk3vrwhBNGN-DxjitQbkwawGm6bO4UssDvZmmvOq3Yp8Qf7UFZVTZRbw4EiI2pdV_Hod8DZkKESeGO9KwSTX7q4KfzxGLu-YMSqIyYkUa-1iSG6LAJmT_uYV56tCbGfQtXEeYEfIQTxu2t03fI5EejSxzsenNRbH1PW1BabHfLHRIndvqcpfx5CQqZ8t9zo0ATK5UqeSl62bx5-J44KHpNiJk-anO60KS4u59eZI3h-SX6ZwiPC9NHUQtxIiP18tULJ2YdI8cCtVvZ1UG0PhZ-0IaVNK-o4fDl5x6fOzTzhtJ3ughjbe_Q-sQ08ahh3pJR4N3jDFEOLFy9lMuiQdVl-YKgsonxCXCujS-pmK9SBVtcKFdQzl_Lxq1q5T4V1ac0d2xLL8hKa7f57SJNM_Dbsq34D4qZkRjq8-v4Ypo_sTUVA3xqA-HVj01sbk1s8Y8SDHDf0lOd0y1xARfQpxuLX2lkc9vddvzyoVDhfjamTPteKzQBIErBfRwXlayRT5AEoMD0bWDOEHWVts-PvkyehnSWRv0rig-rADH5PL4TiW8m-kVIGT-4Aj3e48eKA26JGAq-N1L7HJ3N3m_k2Gn6guSkMDGndY6rL0Fh9W87Bj3mQV3veU6m37Bz2wDRBecpnLrHcqWiHMqtGy49Zb6Mcx5r2STVsYVuXjuNkEGJyv4R5MZEMyGEOwQuwHxU7VMS_UVzGvhM5_uaNwXc3TZf-tcCZjcD9ih2SwtlbLT6EdYDKJetOAe3unNE7Y9D3AOKY4WbXnU1pAGhSZ6OC1c0sNMXLc4np7k210X8y-yXAV02htjrZ9n93fZ7BCiFAHAu09CblmTsMqnvJCpIhoW-_2Y-MLAj_272-q_l4zr0KiOSFGb6uiRW4fJjYw80QqrIZ9h5yQla9yV5zC6qcAGVuIQpGMv1jtRw9Xa8hDS2PO5ggjFyjCm9cQp3Z3DurAwPuxikiLwNNdTgjLwGD9AboWVCzVi3dQI9eqVKrRMzATToZWFQz0xK4-CmjuOckSimo06CTg7V9oUyH3AKGg1P1hyaJJlGHNT25N9ivbAYAKPt2Rie4-P3nTD-RYv7DQFoT3bZ2mLD8dXBT8uusMCfu3H9Hm4VpD1xhqb3occQzE8.tBoLaIMV0kMSk3OAcVUGMdFX_9u3OMoAFF5fFW6CFJo\"")
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