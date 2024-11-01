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

configurations {
    create("combinedShadow")
    create("fabricShadow")
}

dependencies {
    implementation(project(":common"))

    add("combinedShadow", project(":platforms:bukkit"))
    add("combinedShadow", project(":platforms:velocity"))
    add("combinedShadow", project(":platforms:bungeecord"))
    add("combinedShadow", project(":platforms:sponge"))

    add("fabricShadow", project(":platforms:fabric"))
}

fun configureShadowJar(task: ShadowJar, classifier: String?, configurationName: String) {
    task.apply {
        archiveFileName.set("${rootProject.name}${classifier?.let { "-$it" } ?: ""}-${project.version}.jar")
        archiveClassifier = null
        configurations = listOf(project.configurations.getByName(configurationName))

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

tasks {
    jar { enabled = false }

    val combinedShadowJar by creating(ShadowJar::class) {
        configureShadowJar(this, null, "combinedShadow")
    }

    val fabricShadowJar by creating(ShadowJar::class) {
        configureShadowJar(this, "fabric", "fabricShadow")
    }

    assemble { dependsOn(combinedShadowJar, fabricShadowJar) }

    val javaVersion = JavaLanguageVersion.of(21)
    val minecraftVersion = "1.21.1"
    val jvmArgsExternal = listOf("-Dcom.mojang.eula.agree=true")

    val sharedBukkitPlugins = runPaper.downloadPluginsSpec {
        url("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/artifact/spigot/build/libs/packetevents-spigot-2.6.0-SNAPSHOT.jar")
        url("https://github.com/ViaVersion/ViaVersion/releases/download/5.1.0/ViaVersion-5.1.0.jar")
        url("https://github.com/ViaVersion/ViaBackwards/releases/download/5.1.0/ViaBackwards-5.1.0.jar")
    }

    runServer {
        minecraftVersion(minecraftVersion)
        runDirectory.set(rootDir.resolve("run/paper/$minecraftVersion"))
        javaLauncher = project.javaToolchains.launcherFor { languageVersion = javaVersion }
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
        javaLauncher = project.javaToolchains.launcherFor { languageVersion = javaVersion }
        jvmArgs = jvmArgsExternal

        downloadPlugins { from(sharedBukkitPlugins) }
    }

    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")
        runDirectory.set(rootDir.resolve("run/velocity"))
        javaLauncher = project.javaToolchains.launcherFor { languageVersion = javaVersion }
        jvmArgs = jvmArgsExternal

        downloadPlugins {
            url("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/artifact/velocity/build/libs/packetevents-velocity-2.6.0-SNAPSHOT.jar")
            url("https://ci.lucko.me/job/spark/418/artifact/spark-velocity/build/libs/spark-1.10.73-velocity.jar")
        }
    }
}
