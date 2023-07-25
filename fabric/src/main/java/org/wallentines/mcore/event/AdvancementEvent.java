package org.wallentines.mcore.event;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;

/**
 * An event fired when a player makes an advancement
 */
public record AdvancementEvent(ServerPlayer player, Advancement advancement) { }
