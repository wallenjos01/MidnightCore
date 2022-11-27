package org.wallentines.midnightcore.fabric.module.messaging;

import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessageResponse;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

public class FabricResponse implements MessageResponse {

    private final FriendlyByteBuf buffer;

    public FabricResponse(@Nullable FriendlyByteBuf data) {
        this.buffer = data;
    }

    public FriendlyByteBuf getBuffer() {
        return buffer;
    }

    @Override
    public byte[] getRawData() {
        return buffer == null ? null : buffer.array();
    }
    @Override
    public ConfigSection parseConfigSection() {

        if(buffer == null) return null;
        try {
            return JsonConfigProvider.INSTANCE.loadFromString(buffer.readUtf());
        } catch (JsonParseException ex) {

            MidnightCoreAPI.getLogger().warn("Unable to parse plugin message!");
            ex.printStackTrace();
            return null;
        }
    }
}
