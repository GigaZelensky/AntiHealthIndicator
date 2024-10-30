plugins {
    antihealthindicator.`java-conventions`
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

dependencies {
    implementation(project(":common"))
    include(libs.snakeyaml)

    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
    })

    modImplementation("me.lucko:fabric-permissions-api:0.3.3")
    modCompileOnly(libs.packetevents.fabric)
    modCompileOnly("net.fabricmc:fabric-loader:${property("loader_version")}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

loom {
    serverOnlyMinecraftJar()
    mods {
        register("antihealthindicator") {
            sourceSet(sourceSets.main.get())
        }
    }
}
