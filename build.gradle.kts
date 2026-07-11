plugins {
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}

subprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-script-runtime" && requested.version == "1.9.23") {
                useVersion("1.9.20")
                because("This workstation already has 1.9.20 cached, while 1.9.23 download fails behind the current TLS path.")
            }
            if (requested.group == "com.google.code.findbugs" && requested.name == "jsr305") {
                useVersion("3.0.2")
                because("Android test dependencies request 2.0.2, while 3.0.2 is the compatible maintained artifact available locally.")
            }
            if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-coroutines")) {
                useVersion("1.8.1")
                because("Use one compatible coroutines version across the app and Android test tooling.")
            }
            if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-stdlib") {
                useVersion("1.9.23")
                because("Keep Kotlin stdlib aligned with the Kotlin plugin and avoid unavailable intermediate versions in test tooling.")
            }
        }
    }
}
