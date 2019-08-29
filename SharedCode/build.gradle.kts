plugins {
    //kotlin("multiplatform")
    //kotlin("native.cocoapods")
    //kotlin("kotlinx-serialization")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.native.cocoapods")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.allopen")
}

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://kotlin.bintray.com/kotlin/kotlinx") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

group = "org.jetbrains.kotlin.sample.native"
version = "1.0"

kotlin {
    //select iOS target platform depending on the Xcode environment variables
    /*val iOSTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iOSTarget("ios") {
        binaries {
            framework {
                baseName = "SharedCode"
            }
        }
    }*/

    val buildForDevice = project.findProperty("kotlin.native.cocoapods.target") == "ios_arm"
    if (buildForDevice) {
        iosArm64("iOS64").binaries {
            framework {
                baseName = "SharedCode"
                embedBitcode("disable")
            }
            executable()
        }
        iosArm32("iOS32").binaries {
            framework {
                baseName = "SharedCode"
                embedBitcode("disable")
            }
            executable()
        }

        val iOSMain by sourceSets.creating
        sourceSets["iOS64Main"].dependsOn(iOSMain)
        sourceSets["iOS32Main"].dependsOn(iOSMain)
    } else {
        iosX64("iOS").binaries {
            executable()
        }
    }

    /*iosArm64("iOS64").binaries {
        executable()
    }
    iosArm32("iOS32").binaries {
        executable()
    }

    val iOSMain by sourceSets.creating
    sourceSets["iOS64Main"].dependsOn(iOSMain)
    sourceSets["iOS32Main"].dependsOn(iOSMain)

    iosX64("iOS").binaries {
        executable()
    }*/

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Working with AFNetworking from Kotlin/Native using CocoaPods"
        homepage = "https://github.com/JetBrains/kotlin-native"

        // Configure a dependency on AFNetworking. It will be added in all macOS and iOS targets.
        pod("HTMLKit")
        pod("SBJson5")
        //pod("AFNetworking")
        pod("AFNetworking", "~> 3.2.0")
        //pod("Alamofire-Synchronous")
        //pod("SwiftSoup")
        //pod("Kanna", "~> 5.0.0")
        //pod("Ji")/
        //pod("Fuzi")
    }

    /*iOSTarget("ios") {
        binaries {
            cocoapods {
                // Configure fields required by CocoaPods.
                summary = "Working with AFNetworking from Kotlin/Native using CocoaPods"
                homepage = "https://github.com/JetBrains/kotlin-native"

                // Configure a dependency on AFNetworking. It will be added in all macOS and iOS targets.
                pod("HTMLKit")
                pod("AFNetworking", "~> 3.2.0")
                //pod("SwiftSoup")
                //pod("Kanna", "~> 5.0.0")
                //pod("Ji")
                //pod("Fuzi")
            }
        }
    }*/

    jvm("android")

    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.12.0")
        //implementation("com.russhwolf:multiplatform-settings:0.3.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.0")
        //implementation("io.ktor:ktor-client-core:1.2.3")
    }

    sourceSets["androidMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jsoup:jsoup:1.12.1")
        implementation("com.google.code.gson:gson:2.8.5")
        implementation("com.squareup.okhttp3:okhttp:4.1.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.12.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0")
        //implementation("io.ktor:ktor-client-android:1.2.3")
    }

    sourceSets["iOSMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:0.12.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.0")
        //implementation("io.ktor:ktor-client-ios:1.2.3")
    }

}

/*configurations {
    kotlinCompilerClasspath
}*/

/*
val packForXcode by tasks.creating(Sync::class) {
    group = "build"

    //selecting the right configuration for the iOS framework depending on the Xcode environment variables
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)

    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)

    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText("#!/bin/bash\nexport 'JAVA_HOME=${System.getProperty("java.home")}'\ncd '${rootProject.rootDir}'\n./gradlew \$@\n")
        gradlew.setExecutable(true)
    }
}

tasks.getByName("build").dependsOn(packForXcode)
*/
