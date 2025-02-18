/*
 * This file is part of AntiHealthIndicator - https://github.com/Bram1903/AntiHealthIndicator
 * Copyright (C) 2025 Bram and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.deathmotion.antihealthindicator.data.cache;

import com.deathmotion.antihealthindicator.data.AHIPlayer;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RidableEntity extends CachedEntity {
    private float health;
    private int passengerId;

    @Override
    public void processMetaData(EntityData metaData, AHIPlayer player) {
        if (metaData.getIndex() == player.metadataIndex.HEALTH) {
            setHealth((float) metaData.getValue());
        }
    }
}