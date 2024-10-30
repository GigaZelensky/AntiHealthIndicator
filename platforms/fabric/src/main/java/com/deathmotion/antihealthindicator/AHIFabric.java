package com.deathmotion.antihealthindicator;

import com.deathmotion.antihealthindicator.loader.SnakeYamlLoader;
import com.deathmotion.antihealthindicator.schedulers.FabricScheduler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class AHIFabric implements DedicatedServerModInitializer {
    public static final String MOD_ID = "antihealthindicator";

    private FabricAntiHealthIndicator ahi;

    @Override
    public void onInitializeServer() {
        SnakeYamlLoader.loadSnakeYaml();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onEnable);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onDisable);
    }

    private void onEnable(@NotNull MinecraftServer server) {
        this.ahi = new FabricAntiHealthIndicator(FabricLoader.getInstance(), server);

        ahi.commonOnInitialize();

        ahi.setScheduler(new FabricScheduler());
        ahi.commonOnEnable();
    }

    private void onDisable(@NotNull MinecraftServer server) {
        ahi.commonOnDisable();
    }
}