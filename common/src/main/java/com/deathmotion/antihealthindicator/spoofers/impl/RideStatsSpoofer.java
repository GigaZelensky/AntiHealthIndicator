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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.deathmotion.antihealthindicator.spoofers.impl;

import com.deathmotion.antihealthindicator.cache.EntityCache;
import com.deathmotion.antihealthindicator.cache.entities.RidableEntity;
import com.deathmotion.antihealthindicator.data.AHIPlayer;
import com.deathmotion.antihealthindicator.data.Settings;
import com.deathmotion.antihealthindicator.spoofers.Spoofer;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.attribute.Attribute;
import com.github.retrooper.packetevents.protocol.entity.attribute.AttributeModifier;
import com.github.retrooper.packetevents.protocol.entity.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import java.util.List;

/**
 * Spoofs rideable entity attributes like speed and jump strength so that clients
 * cannot read the real values using hacks such as RideStats.
 */
public class RideStatsSpoofer extends Spoofer {

    private final EntityCache cache;

    public RideStatsSpoofer(AHIPlayer player) {
        super(player);
        this.cache = player.entityCache;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.UPDATE_ATTRIBUTES) return;

        Settings.EntityData.RideStats settings = configManager.getSettings().getEntityData().getRideStats();
        if (settings == null || !settings.isEnabled()) return;

        WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(event);
        int entityId = packet.getEntityId();
        if (!cache.isRideableVehicle(entityId)) return;

        // if rider is the receiving player, send real attributes
        int riderId = cache.getPassengerId(entityId);
        if (riderId == player.user.getEntityId()) {
            updateCache(packet, entityId);
            return;
        }

        // otherwise spoof values and update cache with real ones
        updateCache(packet, entityId);
        List<Attribute> attrs = packet.getAttributes();
        for (Attribute attribute : attrs) {
            String key = attribute.getKey();
            if (Attributes.GENERIC_MOVEMENT_SPEED.equals(key)) {
                overwrite(attribute, settings.getSpeed());
            } else if (Attributes.HORSE_JUMP_STRENGTH.equals(key)) {
                overwrite(attribute, settings.getJumpStrength());
            } else if (Attributes.LLAMA_STRENGTH.equals(key)) {
                overwrite(attribute, settings.getLlamaInventorySlots());
            }
        }
        packet.setAttributes(attrs);
        event.markForReEncode(true);
    }

    private void overwrite(Attribute attribute, double value) {
        for (AttributeModifier mod : attribute.getModifiers()) {
            mod.setAmount(value);
        }
        attribute.setBaseValue(value);
    }

    private void updateCache(WrapperPlayServerUpdateAttributes packet, int entityId) {
        cache.getCache().computeIfPresent(entityId, (id, ce) -> {
            if (ce instanceof RidableEntity) {
                RidableEntity r = (RidableEntity) ce;
                for (Attribute attribute : packet.getAttributes()) {
                    String key = attribute.getKey();
                    if (Attributes.GENERIC_MOVEMENT_SPEED.equals(key)) {
                        r.setMaxSpeed(attribute.getBaseValue());
                    } else if (Attributes.HORSE_JUMP_STRENGTH.equals(key)) {
                        r.setJumpStrength(attribute.getBaseValue());
                    } else if (Attributes.LLAMA_STRENGTH.equals(key)) {
                        r.setLlamaInventorySlots((int) attribute.getBaseValue());
                    }
                }
            }
            return ce;
        });
    }
}
