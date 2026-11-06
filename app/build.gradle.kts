plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "kr.co.daparm.collector"; compileSdk = 35
    defaultConfig { applicationId = "kr.co.daparm.collector"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.4-test" }
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}
