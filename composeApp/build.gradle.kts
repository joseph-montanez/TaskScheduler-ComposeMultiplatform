import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.serialization)
    id("app.cash.sqldelight") version "2.0.1"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        iosTarget.compilations.getByName("main") {
            // Correct placement of cinterops within a target's compilation
            cinterops {
                create("filePicker") {
                    defFile(project.file("src/iosMain/cinterop/filePicker.def"))
                    compilerOpts("-F${project.rootDir}/iosApp")
                }
            }
        }
    }
    
    sourceSets {
        val desktopMain by getting
        val voyagerVersion = "1.0.0"
        

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }
        
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

            implementation("org.springframework.security:spring-security-crypto:6.2.3")

            implementation("app.cash.sqldelight:android-driver:2.0.1")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
//            implementation("androidx.compose.material:material-icons-extended:1.6.4")
            implementation("org.jetbrains.compose.material:material-icons-core:1.6.1") // Check for the latest version
            implementation("org.jetbrains.compose.material:material-icons-extended:1.6.1")
            
            
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            
            implementation("com.benasher44:uuid:0.8.4")
            implementation("commons-logging:commons-logging:1.2")
            
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            

            implementation("app.softwork:kotlinx-serialization-csv:0.0.18")
            implementation("app.softwork:kotlinx-serialization-flf:0.0.18")
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation("app.cash.sqldelight:native-driver:2.0.1")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(kotlin("stdlib"))
            implementation(libs.ktor.client.okhttp)
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")

            implementation("org.springframework.security:spring-security-crypto:6.2.3")

            implementation("com.dorkbox:SystemTray:4.4")
        }
    }
}

android {
    namespace = "com.shabb.taskscheduler"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.shabb.taskscheduler"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}
dependencies {
    implementation(libs.protolite.well.known.types)
}

// macOS Specific System Tray
val compileNativeCode = tasks.register("compileNativeCode") {
    doLast {
        val toolchains = project.extensions.getByType(JavaToolchainService::class.java)
        val toolchain = toolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }.get().metadata.installationPath.asFile
        val jdkPath = toolchain.absolutePath

        val srcDir = "src/macOSMain"
        val outDir = layout.buildDirectory.dir("nativeLibs").get().asFile.absolutePath
        val libName = "libAppearanceObserver.dylib"


        // Create the output directory if it doesn't exist
        File(outDir).mkdirs()

        val command = listOf("clang", "-fobjc-arc", "-dynamiclib", "-o", "$outDir/$libName",
                             "$srcDir/AppearanceObserver.m", "$srcDir/AppearanceCallback.m", "-framework", "Cocoa",
                             "-I$jdkPath/include", "-I$jdkPath/include/darwin")
        println(command.joinToString(" "))

        // Compile the Objective-C source into a dynamic library
        exec {
            commandLine(command)
            //commandLine("clang", "-fobjc-arc", "-dynamiclib", "-o", "$outDir/$libName", "$srcDir/AppearanceObserver.m", "$srcDir/AppearanceCallback.m", "-framework", "Cocoa")
        }
    }
}

//project("composeApp") {
//    tasks.named("desktopRun") {
//        dependsOn(compileNativeCode)
//    }
//}
//
//tasks.named(":desktopRun") {
//    dependsOn(compileNativeCode)
//}

tasks.register("prepareResources") {
    dependsOn(compileNativeCode)
    doLast {
        val resourcesDir = "$projectDir/src/main/resources"
        val outDir = layout.buildDirectory.dir("nativeLibs").get().asFile.absolutePath
        val libName = "libAppearanceObserver.dylib"
        File(resourcesDir).mkdirs()
        File(outDir).listFiles()?.forEach { file ->
            if (file.name.equals(libName)) {
                file.copyTo(File(resourcesDir, file.name), overwrite = true)
            }
        }
    }
}

tasks.register("generateJniHeaders") {
    doLast {
        // Define paths
        val className = "com.shabb.AppearanceManager"

        val classOutputDir = layout.buildDirectory.dir("classes/kotlin/desktop/main").get().asFile.absolutePath
        val headerOutputDir = layout.buildDirectory.dir("generated/jniHeaders").get().asFile.absolutePath

        // Ensure the output directory exists
        File(headerOutputDir).mkdirs()

        // Use `javah` to generate JNI headers, assuming `javah` is available in your environment
        exec {
            commandLine("javah", "-d", headerOutputDir, "-classpath", classOutputDir, className)
        }
    }
}



compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.shabb.taskscheduler"
            packageVersion = "1.0.0"
        }
    }
}


sqldelight {
    databases {
        create("Database") {
            packageName = "com.shabb"
            version = 2
        }
    }
}
