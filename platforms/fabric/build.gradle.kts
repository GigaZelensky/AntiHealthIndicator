plugins {
    antihealthindicator.`java-conventions`
    alias(libs.plugins.fabric.loom)
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.packetevents.fabric)

    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
    })

    modCompileOnly("net.fabricmc:fabric-loader:${property("loader_version")}")
}

loom {
    mods {
        register("antihealthindicator") {
            sourceSet(sourceSets.main.get())
        }
    }
}
