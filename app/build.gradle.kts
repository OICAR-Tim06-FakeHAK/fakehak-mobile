plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

android {
    namespace = "hr.algebra.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "hr.algebra.myapplication"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
}

// ─── Coverage ───────────────────────────────────────────────────────────────
// Run with `./gradlew jacocoTestReport` to generate HTML+XML coverage at
// app/build/reports/jacoco/jacocoTestReport/. Reports the data/business layer; UI/Compose
// noise (Hilt-generated, R.class, BuildConfig, ViewBinding) is excluded.

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates JVM unit-test coverage report (HTML + XML)."
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val excludes = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*",
        "**/databinding/**", "**/*_HiltModules*.*", "**/*_Factory.*",
        "**/*_MembersInjector.*", "**/Dagger*Component*.*", "**/Hilt_*.*",
        "**/*_Impl.*", "**/*_GeneratedInjector.*",
        // exclude UI for now — testing scope is data/business only
        "**/fragments/**", "**/adapters/**", "**/dialogs/**", "**/HostActivity*.*",
        "**/HrApp*.*",
    )

    val javaClasses = fileTree("${buildDir}/intermediates/javac/debug/classes") { exclude(excludes) }
    val kotlinClasses = fileTree("${buildDir}/tmp/kotlin-classes/debug") { exclude(excludes) }
    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir) { include("**/*.exec", "**/*.ec") })
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Networking (Retrofit + JSON)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converterGson)
    implementation(libs.okhttp.logging)

    // Async
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel + Compose integration
    implementation(libs.androidx.lifecycle.viewmodelKtx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.security.crypto)

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // JSON
    implementation(libs.gson)

    // Splash screen
    // implementation(libs.androidx.core.splashscreen)

    implementation("androidx.core:core-splashscreen:1.0.1")

    // Background work
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}