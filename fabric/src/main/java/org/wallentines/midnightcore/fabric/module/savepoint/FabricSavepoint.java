package org.wallentines.midnightcore.fabric.module.savepoint;

import com.google.gson.JsonSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.fabric.mixin.AccessorPlayerAdvancements;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.fabric.util.GsonContext;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

public class FabricSavepoint extends AbstractSavepoint {

    private Location location;
    private CompoundTag entityTag;
    private GameType gameMode;
    private AdvancementData advancements;

    protected FabricSavepoint(Identifier id, EnumSet<SaveFlag> flags) {
        super(id, flags);
    }

    public FabricSavepoint(Identifier id, Location location, GameType gameMode, ConfigSection entityTag, AdvancementData advancements, ConfigSection extraData, EnumSet<SaveFlag> flags) {
        super(id, flags);
        this.location = location;
        this.entityTag = ConversionUtil.toCompoundTag(entityTag);
        this.gameMode = gameMode;
        this.advancements = advancements;
        this.extraData = extraData;
    }

    @Override
    public boolean save(MPlayer player) {

        SavepointCreatedEvent event = new SavepointCreatedEvent(this, player);
        Event.invoke(event);

        if(event.isCancelled()) return false;

        ServerPlayer pl = FabricPlayer.getInternal(player);

        if(flags.contains(SaveFlag.LOCATION)) location = player.getLocation();
        if(flags.contains(SaveFlag.DATA_TAG)) entityTag = pl.saveWithoutId(new CompoundTag());
        if(flags.contains(SaveFlag.GAME_MODE)) gameMode = pl.gameMode.getGameModeForPlayer();
        if(flags.contains(SaveFlag.ADVANCEMENTS)) advancements = AdvancementData.save(pl.getAdvancements());

        return true;
    }

    @Override
    public void load(MPlayer player) {

        SavepointLoadedEvent event = new SavepointLoadedEvent(this, player);
        Event.invoke(event);

        if(event.isCancelled()) return;

        ServerPlayer pl = FabricPlayer.getInternal(player);

        if(location != null) player.teleport(location);
        if(entityTag != null) {

            pl.removeAllEffects();
            pl.load(entityTag);
            for(MobEffectInstance inst : pl.getActiveEffects()) {
                pl.connection.send(new ClientboundUpdateMobEffectPacket(pl.getId(), inst));
            }
        }

        if(gameMode != null) pl.setGameMode(gameMode);
        if(advancements != null) advancements.load(pl, pl.getAdvancements());
    }

    public static final Serializer<AdvancementProgress> PROGRESS_SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, AdvancementProgress value) {
            return SerializeResult.success(GsonContext.INSTANCE.convert(context, AccessorPlayerAdvancements.getGson().toJsonTree(value)));
        }

        @Override
        public <O> SerializeResult<AdvancementProgress> deserialize(SerializeContext<O> context, O value) {
            try {
                return SerializeResult.success(AccessorPlayerAdvancements.getGson().fromJson(context.convert(GsonContext.INSTANCE, value), AdvancementProgress.class));
            } catch (JsonSyntaxException ex) {
                return SerializeResult.failure(ex.getMessage());
            }
        }
    };
    public static final InlineSerializer<ResourceLocation> RESOURCE_LOCATION_SERIALIZER = InlineSerializer.of(ResourceLocation::toString, ResourceLocation::new);

    public static final Serializer<FabricSavepoint> SERIALIZER = ObjectSerializer.create(
            Identifier.serializer(MidnightCoreAPI.DEFAULT_NAMESPACE).entry("id",Savepoint::getId),
            Location.SERIALIZER.<FabricSavepoint>entry("location", fs -> fs.location).optional(),
            InlineSerializer.of(GameType::getName, GameType::byName).<FabricSavepoint>entry("gameMode", fs -> fs.gameMode).optional(),
            ConfigSection.SERIALIZER.<FabricSavepoint>entry("tag", fs -> ConversionUtil.toConfigSection(fs.entityTag)).optional(),
            AdvancementData.SERIALIZER.<FabricSavepoint>entry("advancements", fs -> fs.advancements).optional(),
            ConfigSection.SERIALIZER.<FabricSavepoint>entry("extraData", fs -> ConversionUtil.toConfigSection(fs.entityTag)).optional(),
            (id, location, gameMode, entityTag, advancements, extraData) -> {

                Collection<SaveFlag> flags = new ArrayList<>(4);
                if(location != null) flags.add(SaveFlag.LOCATION);
                if(gameMode != null) flags.add(SaveFlag.GAME_MODE);
                if(entityTag != null) flags.add(SaveFlag.DATA_TAG);
                if(advancements != null) flags.add(SaveFlag.ADVANCEMENTS);

                return new FabricSavepoint(id, location, gameMode, entityTag, advancements, extraData, EnumSet.copyOf(flags));
            }
    );

    private record AdvancementData(Map<ResourceLocation, AdvancementProgress> advancements, Integer dataVersion) {

            private AdvancementData(Map<ResourceLocation, AdvancementProgress> advancements, Integer dataVersion) {
                this.advancements = Map.copyOf(advancements);
                this.dataVersion = dataVersion;
            }

            public void load(ServerPlayer player, PlayerAdvancements advancements) {

                MinecraftServer server = player.getServer();
                if (server == null) return;

                ((AdvancementExtension) advancements).loadFromMap(this.advancements, server.getAdvancements());
            }

            public static AdvancementData save(PlayerAdvancements advancements) {

                return new AdvancementData(((AdvancementExtension) advancements).saveToMap(), SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            }

            public static final Serializer<AdvancementData> SERIALIZER = new Serializer<>() {
                @Override
                public <O> SerializeResult<O> serialize(SerializeContext<O> context, AdvancementData value) {

                    SerializeResult<O> res = PROGRESS_SERIALIZER.mapOf(RESOURCE_LOCATION_SERIALIZER).serialize(context, value.advancements);
                    if (!res.isComplete()) return res;

                    O out = res.getOrThrow();
                    if (value.dataVersion != null) context.set("DataVersion", context.toNumber(value.dataVersion), out);

                    return SerializeResult.success(out);
                }

                @Override
                public <O> SerializeResult<AdvancementData> deserialize(SerializeContext<O> context, O value) {

                    SerializeResult<Map<ResourceLocation, AdvancementProgress>> map = PROGRESS_SERIALIZER.mapOf(RESOURCE_LOCATION_SERIALIZER).deserialize(context, value);
                    if (!map.isComplete()) return SerializeResult.failure(map.getError());

                    O num = context.get("DataVersion", value);
                    Integer version = num == null ? null : context.asNumber(num).intValue();

                    return SerializeResult.success(new AdvancementData(map.getOrThrow(), version));
                }
            };
        }


}
