plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.clerami.universe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.clerami.universe"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
            debug {
                buildConfigField ("String", "BASE_URL", "\"https://backend-dot-myproject-441712.et.r.appspot.com/\"") // Development environment
            }
            release {
                buildConfigField ("String", "BASE_URL", "\"https://backend-dot-myproject-441712.et.r.appspot.com/\"") // Production environment
                isMinifyEnabled = true
                proguardFiles (
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    ("proguard-rules.pro")
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
        viewBinding = true
        buildConfig = true
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.9"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.play.services.auth)


    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.auth)
    implementation (libs.firebase.firestore)
    implementation (libs.gms.play.services.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.googleid)

}