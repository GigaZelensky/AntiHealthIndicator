/*
 *  This file is part of AntiHealthIndicator - https://github.com/Bram1903/AntiHealthIndicator
 *  Copyright (C) 2025 Bram and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.deathmotion.antihealthindicator.spoofers.impl;

import com.deathmotion.antihealthindicator.data.AHIPlayer;
import com.deathmotion.antihealthindicator.data.Settings;
import com.deathmotion.antihealthindicator.spoofers.Spoofer;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes.Property;

import java.util.List;

/**
 * Spoofs riding statistics such as max health, speed and jump height of rideable entities.
 */
public final class RideStatsSpoofer extends Spoofer {

    private static final String GENERIC_MAX_HEALTH = "generic.max_health";
    private static final String GENERIC_MOVEMENT_SPEED = "generic.movement_speed";
    private static final String HORSE_JUMP_STRENGTH = "horse.jump_strength";


    public RideStatsSpoofer(AHIPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Settings.RideStats settings = configManager.getSettings().getRideStats();
        if (!settings.isEnabled()) return;

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_PROPERTIES) {
            handleEntityProperties(event, settings);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            handleEntityMetadata(event, settings);
        }
    }

    private void handleEntityProperties(PacketSendEvent event, Settings.RideStats settings) {
        WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(event);
        List<Property> properties = packet.getProperties();
        boolean modified = false;

        for (Property property : properties) {
            String key = property.getKey();
            if (GENERIC_MAX_HEALTH.equals(key) && settings.isMaxHealth()) {
                property.setBaseValue(20.0);
                modified = true;
            } else if (GENERIC_MOVEMENT_SPEED.equals(key) && settings.isSpeed()) {
                property.setBaseValue(0.1);
                modified = true;
            } else if (HORSE_JUMP_STRENGTH.equals(key) && settings.isJumpHeight()) {
                property.setBaseValue(0.5);
                modified = true;
            }
        }

        if (modified) {
            packet.setProperties(properties);
            event.markForReEncode(true);
        }
    }

    private void handleEntityMetadata(PacketSendEvent event, Settings.RideStats settings) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
        EntityType type = packet.getEntityType();
        if (type == null || !type.name().contains("LLAMA")) return;

        List<EntityData<?>> dataList = packet.getEntityMetadata();
        boolean modified = false;

        int llamaIndex = player.metadataIndex.LLAMA_STRENGTH;
        for (EntityData<?> data : dataList) {
            if (data.getIndex() == llamaIndex && settings.isLlamaSlots()) {
                if (data.getType() == EntityDataTypes.INT) {
                    ((EntityData<Integer>) data).setValue(0);
                    modified = true;
                }
            }
        }

        if (modified) {
            packet.setEntityMetadata(dataList);
            event.markForReEncode(true);
        }
    }
}
