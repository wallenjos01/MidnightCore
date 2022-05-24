package org.wallentines.midnightcore.fabric.module.savepoint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricSavepoint extends AbstractSavepoint {

    private Location location;
    private CompoundTag entityTag;
    private GameType gameMode;

    protected FabricSavepoint(Identifier id) {
        super(id);
    }

    @Override
    public boolean save(MPlayer player) {

        SavepointCreatedEvent event = new SavepointCreatedEvent(this, player);
        Event.invoke(event);

        if(event.isCancelled()) return false;

        ServerPlayer pl = FabricPlayer.getInternal(player);

        location = player.getLocation();
        entityTag = pl.saveWithoutId(new CompoundTag());

        gameMode = pl.gameMode.getGameModeForPlayer();
        return true;
    }

    @Override
    public void deserialize(ConfigSection section) {

        location = section.get("location", Location.class);
        gameMode = GameType.byName(section.getString("gameMode"));
        entityTag = ConversionUtil.toCompoundTag(section.getSection("tag"));
        extraData = section.getSection("extraData");

    }

    @Override
    public ConfigSection serialize() {
        return new ConfigSection()
            .with("location", location)
            .with("gameMode", gameMode.getName())
            .with("tag", ConversionUtil.toConfigSection(entityTag))
            .with("extraData", extraData);
    }

    @Override
    public void load(MPlayer player) {

        SavepointLoadedEvent event = new SavepointLoadedEvent(this, player);
        Event.invoke(event);

        if(event.isCancelled()) return;

        ServerPlayer pl = FabricPlayer.getInternal(player);

        player.teleport(location);
        pl.removeAllEffects();

        pl.load(entityTag);
        for(MobEffectInstance inst : pl.getActiveEffects()) {
            pl.connection.send(new ClientboundUpdateMobEffectPacket(pl.getId(), inst));
        }

        pl.setGameMode(gameMode);
    }
}
