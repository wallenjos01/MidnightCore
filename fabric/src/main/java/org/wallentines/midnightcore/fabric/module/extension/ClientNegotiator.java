package org.wallentines.midnightcore.fabric.module.extension;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.fabric.event.client.LoginQueryReceivedEvent;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientNegotiator {


    public static void register(ExtensionModule mod) {

        Event.register(LoginQueryReceivedEvent.class, mod, ev -> {

            // Verify Packet ID
            if(ev.getId().equals(ExtensionModule.SUPPORTED_EXTENSION_PACKET)) {

                // Read server extensions
                FriendlyByteBuf data = ev.getData();
                int serverExtensions = data.readVarInt();

                List<String> ids = new ArrayList<>();

                for(int i = 0 ; i < serverExtensions ; i++) {

                    String id = data.readUtf();
                    if(mod.getLoadedExtensionIds().contains(Identifier.parse(id))) {

                        ids.add(id);
                    }
                }

                // Respond with supported server extensions
                FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
                out.writeVarInt(ids.size());

                for (String s : ids) {
                    out.writeUtf(s);
                }

                ev.respond(out);
            }
        });
    }

}
