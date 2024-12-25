plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.telemedicine"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.telemedicine"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
<<<<<<< HEAD
    buildFeatures {
        dataBinding = true
    }
}


=======
}

>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
<<<<<<< HEAD
    implementation(libs.firebase.storage)
    implementation(libs.mediarouter)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    testImplementation(libs.junit.junit)
    testImplementation(libs.testng)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.testng)
    androidTestImplementation(libs.junit.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation("org.jitsi.react:jitsi-meet-sdk:10.1.2") { isTransitive = true }
    implementation ("com.guolindev.permissionx:permissionx:1.6.1")
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")

=======
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.picasso:picasso:2.8")
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
}