plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.testandoaaplicacao"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testandoaaplicacao"
        minSdk = 24
        targetSdk = 35
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
        compose = true
    }
}

dependencies {

    // --- DEPENDÊNCIAS PRINCIPAIS DO ANDROID E COMPOSE ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.gson)

    // --- DEPENDÊNCIAS DE REDE (PARA O CLIENTE WEBSOCKET) ---
    // Você está usando OkHttp no seu MessageService, então esta é a correta.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // --- DEPENDÊNCIAS DE ARQUITETURA (VIEWMODEL) ---
    // A versão 2.9.1 provavelmente não existe. Usando uma versão estável mais recente.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // --- KOTLIN COROUTINES ---
    // A versão 1.10.1 provavelmente não existe. Usando uma versão estável mais recente.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")


    // --- DEPENDÊNCIAS DE TESTE (Nenhuma alteração aqui) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val ktor_version = "3.2.2"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
}