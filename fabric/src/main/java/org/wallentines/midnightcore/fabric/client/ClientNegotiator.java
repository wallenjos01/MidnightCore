package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightcore.fabric.event.client.LoginQueryReceivedEvent;
import org.wallentines.midnightlib.event.Event;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class ClientNegotiator {

    private final HashMap<ResourceLocation, QueryHandler> handlers = new HashMap<>();

    public ClientNegotiator(ClientExtensionModule mod) {

        Event.register(LoginQueryReceivedEvent.class, mod, ev -> {

            QueryHandler handler = handlers.get(ev.getId());
            if(handler != null) {
                ev.respond(handler.handle(ev.getData()));
            }
        });
    }

    public void registerHandler(ResourceLocation id, QueryHandler handler) {

        handlers.put(id, handler);
    }

    public interface QueryHandler {

        FriendlyByteBuf handle(FriendlyByteBuf input);

    }

}
