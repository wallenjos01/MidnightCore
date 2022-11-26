package org.wallentines.midnightcore.velocity.module.globaljoin;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.player.VelocityPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;

public class GlobalJoinModule implements Module<MidnightCoreAPI> {

    private final HashMap<String, MComponent> serverDisplayNames = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        ConfigSection sec = section.getSection("servers");
        for(String key : sec.getKeys()) {
            serverDisplayNames.put(key, sec.get(key, MComponent.class));
        }

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);

        return true;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onLeave(DisconnectEvent event) {

        for(MPlayer mpl : MidnightCoreAPI.getInstance().getPlayerManager()) {
            mpl.sendMessage(new MTranslateComponent("multiplayer.player.left", VelocityPlayer.wrap(event.getPlayer()).getName()).withStyle(new MStyle().withColor(Color.fromRGBI(14))));
        }

    }

    @Subscribe
    public void onJoinServer(ServerConnectedEvent event) {

        RegisteredServer server = event.getServer();
        String serverName = server.getServerInfo().getName();

        MComponent serverDisplay = serverDisplayNames.getOrDefault(serverName, new MTextComponent(serverName));
        serverDisplay.setHoverEvent(MHoverEvent.createTextHover(new MTextComponent("Click to travel to ").withChild(serverDisplay.copy())));
        serverDisplay.setClickEvent(new MClickEvent(MClickEvent.ClickAction.RUN_COMMAND, "/server " + serverName));

        // Changed Server
        if(event.getPreviousServer().isPresent()) {

            for(MPlayer mpl : MidnightCoreAPI.getInstance().getPlayerManager()) {
                mpl.sendMessage(
                        new MTextComponent("").withStyle(new MStyle().withColor(Color.fromRGBI(14)))
                        .withChild(VelocityPlayer.wrap(event.getPlayer()).getName())
                        .withChild(new MTextComponent(" traveled to server "))
                        .withChild(serverDisplay)
                );
            }
            return;
        }

        // Joined for the first time
        for(MPlayer mpl : MidnightCoreAPI.getInstance().getPlayerManager()) {
            mpl.sendMessage(
                    new MTranslateComponent("multiplayer.player.joined", VelocityPlayer.wrap(event.getPlayer()).getName()).withStyle(new MStyle().withColor(Color.fromRGBI(14)))
                    .withChild(new MTextComponent(" in server "))
                    .withChild(serverDisplay));
        }
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "global_join_messages");

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(
            GlobalJoinModule::new,
            ID,
            new ConfigSection().with("servers", new ConfigSection()
                    .with("hub", new MTextComponent("Hub"))
                    .with("survival", new MTextComponent("Survival").withStyle(new MStyle().withColor(Color.fromRGBI(11))))
                    .with("creative", new MTextComponent("Creative").withStyle(new MStyle().withColor(Color.fromRGBI(10))))
            )
    );

}
