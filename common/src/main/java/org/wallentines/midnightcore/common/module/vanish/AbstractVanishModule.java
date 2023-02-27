package org.wallentines.midnightcore.common.module.vanish;

import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.module.vanish.VanishModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public abstract class AbstractVanishModule implements VanishModule {

    protected boolean hideMessages;
    protected Set<MPlayer> globalVanished = new HashSet<>();
    protected HashMap<MPlayer, Set<MPlayer>> vanished = new HashMap<>();

    protected MServer server;

    @Override
    public boolean initialize(ConfigSection section, MServer server) {

        this.server = server;
        this.hideMessages = section.getBoolean("hide_join_messages");

        Event.register(SavepointCreatedEvent.class, this, ev -> {

            ConfigList uuids = new ConfigList();
            if(vanished.containsKey(ev.getPlayer())) {
                for (MPlayer mpl : vanished.get(ev.getPlayer())) {
                    uuids.add(mpl.getUUID().toString());
                }
            }

            ev.getSavepoint().getExtraData().set(MidnightCoreAPI.DEFAULT_NAMESPACE + "_savepoint", new ConfigSection()
                   .with("global", globalVanished.contains(ev.getPlayer()))
                   .with("vanished", uuids));
        });

        Event.register(SavepointLoadedEvent.class, this, ev -> {

            if(!ev.getSavepoint().getExtraData().has(MidnightCoreAPI.DEFAULT_NAMESPACE + "_savepoint")) return;
            ConfigSection sec = ev.getSavepoint().getExtraData().getSection(MidnightCoreAPI.DEFAULT_NAMESPACE + "_savepoint");

            if(sec.getBoolean("global")) {
                vanishPlayer(ev.getPlayer());
            }
            for(UUID u : sec.getListFiltered("vanished", Serializer.UUID)) {
                MPlayer mpl = server.getPlayerManager().getPlayer(u);
                vanishPlayerFor(mpl, ev.getPlayer());
            }
        });

        registerEvents();

        return true;
    }

    @Override
    public void vanishPlayer(MPlayer player) {

        if(!globalVanished.contains(player)) {
            for(MPlayer observer : server.getPlayerManager()) {
                doVanish(player, observer);
            }
            globalVanished.add(player);
        }
    }

    @Override
    public void vanishPlayerFor(MPlayer player, MPlayer observer) {

        Set<MPlayer> set = vanished.computeIfAbsent(observer, k -> new HashSet<>());
        if(!set.contains(player)) {
            doVanish(player, observer);
            set.add(player);
        }

    }

    @Override
    public void revealPlayer(MPlayer player) {

        if(globalVanished.contains(player)) {
            globalVanished.remove(player);
            for(MPlayer observer : server.getPlayerManager()) {
                doReveal(player, observer);
            }
        }

    }

    @Override
    public void revealPlayerFor(MPlayer player, MPlayer observer) {

        Set<MPlayer> set = vanished.computeIfAbsent(observer, k -> new HashSet<>());
        if(set.contains(player)) {
            set.remove(player);
            doReveal(player, observer);
        }
    }

    protected abstract void doVanish(MPlayer player, MPlayer observer);
    protected abstract void doReveal(MPlayer player, MPlayer observer);

    protected abstract void registerEvents();

    @Override
    public boolean isVanished(MPlayer player) {

        return globalVanished.contains(player);
    }

    @Override
    public boolean isVanishedFor(MPlayer player, MPlayer observer) {

        return vanished.containsKey(observer) && vanished.get(observer).contains(player);
    }

    protected void onJoin(MPlayer player) {
        if(isVanished(player)) {
            for(MPlayer pl : server.getPlayerManager()) {
                doVanish(player, pl);
            }
        }
    }

    protected static final ConfigSection DEFAULTS = new ConfigSection().with("hide_join_messages", true);
    public static final Identifier ID = new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "vanish");

}
