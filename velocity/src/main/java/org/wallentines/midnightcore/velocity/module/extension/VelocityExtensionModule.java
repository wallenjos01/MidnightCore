package org.wallentines.midnightcore.velocity.module.extension;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerLoginPluginMessageEvent;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.module.extension.Extension;
import org.wallentines.midnightcore.common.module.extension.ExtensionModule;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.module.messaging.VelocityMessagingModule;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public class VelocityExtensionModule implements ExtensionModule {

    private final HashMap<String, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();
    private final List<Identifier> supportedExtensions = new ArrayList<>();

    @Override
    public <T extends Extension> T getExtension(Class<T> clazz) {
        throw new IllegalStateException("The Velocity Extension module does not register its own extensions! It merely forwards extensions from client to servers!");
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {
        return supportedExtensions;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        MessagingModule mod = MidnightCoreAPI.getModule(MessagingModule.class);
        if(mod == null) return false;

        supportedExtensions.clear();
        supportedExtensions.addAll(section.getListFiltered("query_modules", String.class).stream().map(str -> Identifier.parseOrDefault(str, Constants.DEFAULT_NAMESPACE)).toList());

        ByteArrayDataOutput supportedData = ByteStreams.newDataOutput();

        AbstractMessagingModule.writeVarInt(supportedData, supportedExtensions.size());
        for(Identifier id : supportedExtensions) {
            supportedData.writeUTF(id.toString());
        }

        mod.addLoginListener(ln -> {
            ln.sendRawMessage(SUPPORTED_EXTENSION_PACKET, supportedData.toByteArray(), res -> {
                handleResponse(ln.getPlayerUsername(), res.getRawData());
            });
        });

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);
        return true;
    }

    @Subscribe
    private void onMessage(ServerLoginPluginMessageEvent event) {

        if(event.getIdentifier().getId().equals(SUPPORTED_EXTENSION_PACKET.toString())) {

            ByteArrayDataInput inp = ByteStreams.newDataInput(event.getContents());

            Set<Identifier> serverIds = new HashSet<>();

            int count = AbstractMessagingModule.readVarInt(inp);
            for(int i = 0 ; i < count ; i++) {
                serverIds.add(Identifier.parseOrDefault(inp.readUTF(), Constants.DEFAULT_NAMESPACE));
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            HashMap<Identifier, Version> ids = enabledExtensions.get(event.getConnection().getPlayer().getUsername());

            for(Map.Entry<Identifier, Version> id : ids.entrySet()) {
                if(!serverIds.contains(id.getKey())) continue;

                out.writeUTF(id.getKey().toString());
                out.writeUTF(id.getValue().toString());
            }

            event.setResult(ServerLoginPluginMessageEvent.ResponseResult.reply(out.toByteArray()));
        }
    }

    private void handleResponse(String user, byte[] res) {

        if(res == null) {

            MidnightCoreAPI.getLogger().info("Player " + user + " Ignored Extensions Packet");

        } else {

            ByteArrayDataInput inp = ByteStreams.newDataInput(res);

            HashMap<Identifier, Version> ids = new HashMap<>();

            int count = AbstractMessagingModule.readVarInt(inp);
            for(int i = 0 ; i < count ; i++) {
                ids.put(Identifier.parseOrDefault(inp.readUTF(), Constants.DEFAULT_NAMESPACE), Version.SERIALIZER.deserialize(inp.readUTF()));
            }

            enabledExtensions.put(user, ids);

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
        }
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO =
            new ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>(VelocityExtensionModule::new, ID, DEFAULT_CONFIG.copy().with("query_extensions", new ArrayList<>()))
                    .dependsOn(VelocityMessagingModule.ID);
}
