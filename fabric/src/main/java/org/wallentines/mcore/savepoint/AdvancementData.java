package org.wallentines.mcore.savepoint;

import com.google.gson.JsonSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.mcore.mixin.AccessorPlayerAdvancements;
import org.wallentines.mdcfg.serializer.*;

import java.util.Map;

/**
 * A data type which stores serialized data about player advancements
 * @param advancements The advancements
 * @param dataVersion The data version by which the advancements were saved
 */
public record AdvancementData(Map<ResourceLocation, AdvancementProgress> advancements, Integer dataVersion) {

    public static final Serializer<AdvancementProgress> PROGRESS_SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, AdvancementProgress value) {
            if(value == null) return SerializeResult.failure("Value was null");
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


    public AdvancementData(Map<ResourceLocation, AdvancementProgress> advancements, Integer dataVersion) {
        this.advancements = Map.copyOf(advancements);
        this.dataVersion = dataVersion;
    }

    /**
     * Restores a player's advancement data from this saved data
     * @param player The player to restore
     */
    public void load(ServerPlayer player) {

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ((AdvancementExtension) player.getAdvancements()).loadFromMap(this.advancements, server.getAdvancements());
    }

    /**
     * Saves a player's advancement data
     * @param advancements The advancements to save
     * @return Saved advancement data
     */
    public static AdvancementData save(PlayerAdvancements advancements) {

        return new AdvancementData(((AdvancementExtension) advancements).saveToMap(), SharedConstants.getCurrentVersion().getDataVersion().getVersion());
    }

    public static final Serializer<AdvancementData> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, AdvancementData value) {

            SerializeResult<O> res = PROGRESS_SERIALIZER.filteredMapOf(RESOURCE_LOCATION_SERIALIZER).serialize(context, value.advancements);
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
