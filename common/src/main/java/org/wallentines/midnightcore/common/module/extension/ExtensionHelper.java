package org.wallentines.midnightcore.common.module.extension;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.module.messaging.PacketBufferUtils;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;
import java.util.function.Function;

public class ExtensionHelper {

    public static final Identifier SUPPORTED_EXTENSION_PACKET = new Identifier(Constants.DEFAULT_NAMESPACE, "supported_extensions");
    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "extension");

    public static ByteBuf createPacket(Collection<Identifier> supported) {
        ByteBuf supportedData = Unpooled.buffer();

        PacketBufferUtils.writeVarInt(supportedData, supported.size());
        for(Identifier id : supported) {
            PacketBufferUtils.writeUtf(supportedData, id.toString());
        }

        return supportedData;
    }

    public static ByteBuf createResponse(ByteBuf query, Collection<Identifier> loaded, Function<Identifier, Version> versions) {

        // Read server extensions
        int serverExtensions = PacketBufferUtils.readVarInt(query);

        List<Identifier> ids = new ArrayList<>();
        for(int i = 0 ; i < serverExtensions ; i++) {

            String s = PacketBufferUtils.readUtf(query);
            Identifier id = Identifier.parse(s);

            if(loaded.contains(id)) {
                ids.add(id);
            }
        }

        // Respond with supported server extensions
        ByteBuf out = Unpooled.buffer();
        PacketBufferUtils.writeVarInt(out, ids.size());

        for (Identifier s : ids) {
            PacketBufferUtils.writeUtf(out, s.toString());

            Version ver = versions.apply(s);
            PacketBufferUtils.writeUtf(out, ver.toString());
        }

        return out;
    }

    public static  HashMap<Identifier, Version> handleResponse(String user, ByteBuf res) {

        if(res == null) {

            MidnightCoreAPI.getLogger().info("Player " + user + " Ignored Extensions Packet");
            return null;

        } else {

            HashMap<Identifier, Version> ids = new HashMap<>();

            int count = PacketBufferUtils.readVarInt(res);
            for(int i = 0 ; i < count ; i++) {
                String str = PacketBufferUtils.readUtf(res);
                String ver = PacketBufferUtils.readUtf(res);

                ids.put(Identifier.parseOrDefault(str, Constants.DEFAULT_NAMESPACE), Version.SERIALIZER.deserialize(ver));
            }

            StringBuilder bld = new StringBuilder("Enabled Extensions for ").append(user).append(": ");
            int i = 0;
            for(Map.Entry<Identifier, Version> id : ids.entrySet()) {
                if(i > 0) {
                    bld.append(", ");
                }
                bld.append(id.getKey()).append(": ").append(id.getValue());
                i++;
            }

            MidnightCoreAPI.getLogger().info(bld.toString());

            return ids;
        }
    }

}
