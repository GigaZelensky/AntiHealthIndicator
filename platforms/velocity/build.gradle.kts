plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.0"
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.github.retrooper.packetevents:velocity:2.3.0")
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
}

tasks.register("generateTemplates") {}

tasks {
    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")
        runDirectory.set(file("server/velocity/"))

        javaLauncher.set(project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        })

        downloadPlugins {
            url("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/artifact/velocity/build/libs/packetevents-velocity-2.3.1-SNAPSHOT.jar")
        }
    }
}
