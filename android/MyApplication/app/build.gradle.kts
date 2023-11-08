plugins {
    id("com.android.application")
    id ("com.google.gms.google-services")
}

android {

    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 33
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
    packagingOptions{
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/io.netty.versions.properties")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // JSON 데이터를 파싱하기 위한 Gson 변환기
    implementation ("mysql:mysql-connector-java:5.1.49")
    //implementation ("com.amazonaws:aws-java-sdk-s3:1.11.1023") // S3에 대한 의존성
    //implementation ("com.amazonaws:aws-java-sdk-core:1.11.1023") // 기본 AWS SDK 의존성
    // 8.0.33, 5.7.43
    implementation ("com.google.code.gson:gson:2.8.8")
    //implementation (files("libs/mysql-connector-java-8.0.33.jar"))
    /////////////////////////////////////////////////////////////////////////

    //implementation ("com.amazonaws:aws-android-sdk-cognito:2.13.5")
    //implementation ("com.amazonaws:aws-android-sdk-s3:2.13.5")
    //implementation ("com.amazonaws:aws-android-sdk-core:2.13.5")
    //implementation ("com.amazonaws:aws-android-sdk-s3:2.13.5")
    //implementation ("software.amazon.awssdk:bom:2.13.5")
    //implementation ("software.amazon.awssdk:s3:2.21.1")
    implementation ("androidx.appcompat:appcompat:1.4.1")
    implementation ("com.google.android.material:material:1.4.1")

    implementation("com.google.firebase:firebase-bom:32.3.1")

    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation ("com.google.firebase:firebase-messaging:23.2.1")
    implementation ("com.google.firebase:firebase-inappmessaging-ktx:20.3.5")
    implementation ("com.google.firebase:firebase-inappmessaging-display-ktx:20.3.5")
    implementation ("com.google.firebase:firebase-analytics")

    // 코루틴
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    // 메일 보내기
    implementation (files("libs/activation.jar"))
    implementation (files("libs/additionnal.jar"))
    implementation (files("libs/mail.jar"))
}
