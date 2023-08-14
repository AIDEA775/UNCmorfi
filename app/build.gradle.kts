plugins{
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
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

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // AndroidX
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Database
    val roomVersion = "2.5.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Material Design
    implementation("com.google.android.material:material:1.9.0")

    // Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Load images
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Rest client
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.8.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.2")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.0.2")

    // Parse HTML
    implementation("org.jsoup:jsoup:1.14.3")

    // Java 8 API (for Java Time classes)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.google.truth:truth:1.0.1")

}