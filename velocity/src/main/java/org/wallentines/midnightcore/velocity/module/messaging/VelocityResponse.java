package org.wallentines.midnightcore.velocity.module.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.gson.JsonParseException;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessageResponse;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

public class VelocityResponse implements MessageResponse {

    private final PluginMessageEvent event;

    public VelocityResponse(PluginMessageEvent event) {
        this.event = event;
    }

    @Override
    public byte[] getRawData() {
        return event.getData();
    }

    @Override
    public ConfigSection parseConfigSection() {

        ByteArrayDataInput is = event.dataAsDataStream();
        try {
            return JsonConfigProvider.INSTANCE.loadFromString(is.readUTF());
        } catch (JsonParseException ex) {

            MidnightCoreAPI.getLogger().warn("Unable to parse plugin message!");
            ex.printStackTrace();
            return null;
        }
    }
}
