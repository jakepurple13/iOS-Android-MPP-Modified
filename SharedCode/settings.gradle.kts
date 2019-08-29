pluginManagement {
    repositories {
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }

    resolutionStrategy {
        val kotlin_version: String = "1.3.50"// by settings
        eachPlugin {
            when {
                requested.id.id == "org.jetbrains.kotlin.native.cocoapods" ||
                        requested.id.id == "kotlin-native-cocoapods" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
                requested.id.id.startsWith("org.jetbrains.kotlin") ->
                    useVersion(kotlin_version)
                requested.id.id == "kotlinx-serialization" -> {
                    useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
                }
            }
        }
    }
}

enableFeaturePreview("GRADLE_METADATA")