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
        }
    }
}
