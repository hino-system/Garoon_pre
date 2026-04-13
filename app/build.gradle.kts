plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.garoon_pre"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.garoon_pre"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "PREFERENCE_BACKEND", "\"LOCAL\"")

        testInstrumentationRunner = "com.example.garoon_pre.HiltTestRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "PREFERENCE_BACKEND", "\"LOCAL\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "PREFERENCE_BACKEND", "\"API\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "backend"
    productFlavors {
        create("local") {
            dimension = "backend"
            buildConfigField("String", "PREFERENCE_BACKEND", "\"LOCAL\"")
        }
        create("api") {
            dimension = "backend"
            buildConfigField("String", "PREFERENCE_BACKEND", "\"API\"")
        }
        create("aws") {
            dimension = "backend"
            buildConfigField("String", "PREFERENCE_BACKEND", "\"AWS\"")
        }
        create("azure") {
            dimension = "backend"
            buildConfigField("String", "PREFERENCE_BACKEND", "\"AZURE\"")
        }
        create("gcp") {
            dimension = "backend"
            buildConfigField("String", "PREFERENCE_BACKEND", "\"GCP\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:designsystem"))

    implementation(project(":feature:auth:ui"))
    implementation(project(":feature:auth:data"))
    implementation(project(":feature:board:ui"))
    implementation(project(":feature:board:data"))
    implementation(project(":feature:schedule:ui"))
    implementation(project(":feature:schedule:data"))
    implementation(project(":feature:availability:ui"))
    implementation(project(":feature:availability:data"))
    implementation(project(":feature:home:ui"))
    implementation(project(":sync"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.foundation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
}

kapt {
    correctErrorTypes = true
}