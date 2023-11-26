plugins {
    id("com.android.application")
}

repositories {
    mavenCentral()
    google()
}

android {
    namespace = "com.akoscz.youtube"
    compileSdk = 34
    flavorDimensions += "default"

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    buildTypes {
        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
    }
    productFlavors {
        create("defaultFlavor") {
            proguardFiles("proguard-rules.txt")
        }
    }
    configurations {
        all {
            exclude(module = "httpclient")
            exclude(module = "commons-logging")
        }
    }
}

dependencies {
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.apis:google-api-services-youtube:v3-rev20231011-2.0.0")
    implementation("com.google.http-client:google-http-client-android:1.43.3")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.api-client:google-api-client-gson:2.2.0")
}