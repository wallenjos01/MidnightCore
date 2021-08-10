package me.m1dnightninja.midnightcore.velocity.module.pluginmessage;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;
import me.m1dnightninja.midnightcore.velocity.player.VelocityPlayer;

public class PluginMessageModule implements IModule {

    private static final MIdentifier ID = MIdentifier.create("midnightcore", "plugin_message");

    private final MRegistry<PluginMessageHandler> registry = new MRegistry<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);

        registerListener(MIdentifier.create("midnightcore", "send"), (player, data) -> {

            String serverName = data.readUTF();
            MidnightCore.getInstance().getServer().getServer(serverName).ifPresent(server -> ((VelocityPlayer) player).getVelocityPlayer().createConnectionRequest(server));
        });

        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }

    public void registerListener(MIdentifier id, PluginMessageHandler handler) {

        registry.register(id, handler);
        MidnightCore.getInstance().getServer().getChannelRegistrar().register(MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath()));

    }

    @Subscribe
    private void onMessage(PluginMessageEvent event) {

        MIdentifier id = MIdentifier.parse(event.getIdentifier().getId());

        if(!registry.contains(id) || !(event.getTarget() instanceof Player)) return;
        try {
            registry.get(id).handle(VelocityPlayer.wrap((Player) event.getTarget()), event.dataAsDataStream());

        } catch (Exception ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while handling a plugin message!");
            ex.printStackTrace();
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

}
