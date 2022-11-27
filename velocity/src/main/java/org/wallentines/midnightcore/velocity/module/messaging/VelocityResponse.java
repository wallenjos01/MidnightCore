package org.wallentines.midnightcore.velocity.module.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonParseException;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessageResponse;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

public class VelocityResponse implements MessageResponse {

    private final byte[] data;

    public VelocityResponse(PluginMessageEvent event) {
        this.data = event.getData();
    }

    public VelocityResponse(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getRawData() {
        return data;
    }

    @Override
    public ConfigSection parseConfigSection() {

        ByteArrayDataInput is = ByteStreams.newDataInput(data);
        try {
            return JsonConfigProvider.INSTANCE.loadFromString(is.readUTF());
        } catch (JsonParseException ex) {

            MidnightCoreAPI.getLogger().warn("Unable to parse plugin message!");
            ex.printStackTrace();
            return null;
        }
    }
}
