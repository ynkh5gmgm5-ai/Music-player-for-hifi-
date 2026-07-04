plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yuandao.music"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yuandao.music"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

val debugUnitTestClasspathRoot = File(System.getProperty("java.io.tmpdir"), "yuandao-music-unit-test-classes")

val prepareDebugUnitTestClasspath by tasks.registering(org.gradle.api.tasks.Sync::class) {
    dependsOn(
        "compileDebugKotlin",
        "compileDebugUnitTestKotlin",
        "compileDebugJavaWithJavac",
        "compileDebugUnitTestJavaWithJavac",
    )
    into(debugUnitTestClasspathRoot)
    from(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        into("debug")
    }
    from(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
        into("debug")
    }
    from(layout.buildDirectory.dir("tmp/kotlin-classes/debugUnitTest")) {
        into("debugUnitTest")
    }
    from(layout.buildDirectory.dir("intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes")) {
        into("debugUnitTest")
    }
}

afterEvaluate {
    tasks.named<org.gradle.api.tasks.testing.Test>("testDebugUnitTest").configure {
        dependsOn(prepareDebugUnitTestClasspath)
        val mainClasses = debugUnitTestClasspathRoot.resolve("debug")
        val testClasses = debugUnitTestClasspathRoot.resolve("debugUnitTest")
        testClassesDirs = files(testClasses)
        classpath = files(testClasses, mainClasses) + classpath
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.documentfile)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
