package com.deathmotion.antihealthindicator;

import com.deathmotion.antihealthindicator.interfaces.Scheduler;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import lombok.Getter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class FabricAntiHealthIndicator extends AHIPlatform<FabricLoader>{

    private final FabricLoader plugin;
    private final MinecraftServer server;
    private final Logger logger = LogManager.getLogger(AHIFabric.MOD_ID);

    public FabricAntiHealthIndicator(FabricLoader plugin, @NotNull MinecraftServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public FabricLoader getPlatform() {
        return this.plugin;
    }

    @Override
    public boolean hasPermission(UUID sender, String permission) {
        ServerPlayer player = server.getPlayerList().getPlayer(sender);
        if (player == null) return false;

        return Permissions.check(player, permission);
    }

    @Override
    public void sendConsoleMessage(Component message) {
        logger.info(LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Override
    public String getPluginDirectory() {
        return plugin.getConfigDir().resolve(AHIFabric.MOD_ID).toString();
    }

    protected void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
