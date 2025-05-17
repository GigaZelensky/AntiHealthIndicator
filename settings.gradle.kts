pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}


rootProject.name = "AntiHealthIndicator"
include(":api")
include(":common")
include(":platforms:bukkit")
include(":platforms:velocity")
include(":platforms:bungeecord")
include(":platforms:sponge")
include(":tests:api-bukkit-test-plugin")
include(":tests:api-velocity-test-plugin")
