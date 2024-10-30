import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    antihealthindicator.`java-conventions`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.run.velocity)
}

group = "com.deathmotion.antihealthindicator"
description = "A plugin that prevents hackers and modders from seeing the health of other players."
version = "2.2.5-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation(project(":platforms:bukkit"))
    implementation(project(":platforms:velocity"))
    implementation(project(":platforms:bungeecord"))
    implementation(project(":platforms:sponge"))
    implementation(project(":platforms:fabric"))
}

fun configureShadowJar(task: ShadowJar, classifier: String?, excludeFabric: Boolean) {
    task.apply {
        archiveFileName.set("${rootProject.name}-${project.version}${classifier?.let { "-$it" } ?: ""}.jar")
        archiveClassifier = classifier
        configurations = listOf(project.configurations.runtimeClasspath.get())

        dependencies {
            if (excludeFabric) exclude(project(":platforms:fabric"))
            else {
                exclude(project(":platforms:bukkit"))
                exclude(project(":platforms:bungeecord"))
                exclude(project(":platforms:velocity"))
                exclude(project(":platforms:sponge"))
            }
        }

        relocate(
            "net.kyori.adventure.text.serializer.gson",
            "io.github.retrooper.packetevents.adventure.serializer.gson"
        )
        relocate(
            "net.kyori.adventure.text.serializer.legacy",
            "io.github.retrooper.packetevents.adventure.serializer.legacy"
        )

        manifest { attributes["Implementation-Version"] = rootProject.version }
    }
}

val javaVersion = JavaLanguageVersion.of(21)
val minecraftVersion = "1.21.1"
val jvmArgsExternal = listOf("-Dcom.mojang.eula.agree=true")

val sharedBukkitPlugins = runPaper.downloadPluginsSpec {
    url("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/artifact/spigot/build/libs/packetevents-spigot-2.6.0-SNAPSHOT.jar")
    url("https://github.com/ViaVersion/ViaVersion/releases/download/5.1.0/ViaVersion-5.1.0.jar")
    url("https://github.com/ViaVersion/ViaBackwards/releases/download/5.1.0/ViaBackwards-5.1.0.jar")
}

tasks {
    jar { enabled = false }

    val combinedShadowJar by creating(ShadowJar::class) {
        configureShadowJar(this, null, excludeFabric = true)
    }

    val fabricShadowJar by creating(ShadowJar::class) {
        configureShadowJar(this, "fabric", excludeFabric = false)
    }

    assemble { dependsOn(combinedShadowJar, fabricShadowJar) }

    runServer {
        minecraftVersion(minecraftVersion)
        runDirectory.set(rootDir.resolve("run/paper/$minecraftVersion"))
        javaLauncher.set(project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) })
        jvmArgs = jvmArgsExternal

        downloadPlugins {
            from(sharedBukkitPlugins)
            url("https://ci.lucko.me/job/spark/462/artifact/spark-bukkit/build/libs/spark-1.10.116-bukkit.jar")
            url("https://download.luckperms.net/1560/bukkit/loader/LuckPerms-Bukkit-5.4.145.jar")
        }
    }

    runPaper.folia.registerTask {
        minecraftVersion(minecraftVersion)
        runDirectory.set(rootDir.resolve("run/folia/$minecraftVersion"))
        javaLauncher.set(project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) })
        jvmArgs = jvmArgsExternal

        downloadPlugins { from(sharedBukkitPlugins) }
    }

    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")
        runDirectory.set(rootDir.resolve("run/velocity"))
        javaLauncher.set(project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) })
        jvmArgs = jvmArgsExternal

        downloadPlugins {
            url("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/artifact/velocity/build/libs/packetevents-velocity-2.6.0-SNAPSHOT.jar")
            url("https://ci.lucko.me/job/spark/418/artifact/spark-velocity/build/libs/spark-1.10.73-velocity.jar")
        }
    }
}
