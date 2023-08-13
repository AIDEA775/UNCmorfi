plugins{
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android-extensions")
}

android {
    namespace = "com.uncmorfi"

    compileSdk = 33

    defaultConfig {
        applicationId = "com.uncmorfi"
        minSdk = 23
        targetSdk = 33
        versionCode = 16
        versionName = "v7.1 Garbanzo"
        resValue("string", "app_name", "UNCmorfi")

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas".toString())
            }
        }
    }
    buildTypes {
        getByName("release"){
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug"){
            isDebuggable = true
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "UNCmorfi DEBUG")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // AndroidX
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    // Database
    val room_version = "2.4.3"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Material Design
    implementation("com.google.android.material:material:1.6.0")

    // Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Load images
    implementation("com.github.bumptech.glide:glide:4.11.0")

    // Rest client
    implementation("com.squareup.retrofit2:retrofit:2.8.1")
    implementation("com.squareup.retrofit2:converter-gson:2.8.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.7.2")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.0.2")

    // Parse HTML
    implementation("org.jsoup:jsoup:1.14.3")

    // Java 8 API (for Java Time classes)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.google.truth:truth:1.0.1")

}