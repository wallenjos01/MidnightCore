package org.wallentines.midnightcore.fabric.module.savepoint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricSavepoint extends AbstractSavepoint {

    private Location location;
    private CompoundTag entityTag;
    private GameType gameMode;

    protected FabricSavepoint(Identifier id) {
        super(id);
    }

    public FabricSavepoint(Identifier id, Location location, GameType gameMode, ConfigSection entityTag, ConfigSection extraData) {
        super(id);
        this.location = location;
        this.entityTag = ConversionUtil.toCompoundTag(entityTag);
        this.gameMode = gameMode;
        this.extraData = extraData;
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

    public static final Serializer<FabricSavepoint> SERIALIZER = ObjectSerializer.create(
            Identifier.serializer(MidnightCoreAPI.DEFAULT_NAMESPACE).entry("id",Savepoint::getId),
            Location.SERIALIZER.entry("location", fs -> fs.location),
            InlineSerializer.of(GameType::getName, GameType::byName).entry("gameMode", fs -> fs.gameMode),
            ConfigSection.SERIALIZER.entry("tag", fs -> ConversionUtil.toConfigSection(fs.entityTag)),
            ConfigSection.SERIALIZER.<FabricSavepoint>entry("extraData", fs -> ConversionUtil.toConfigSection(fs.entityTag)).optional(),
            FabricSavepoint::new
    );
}
