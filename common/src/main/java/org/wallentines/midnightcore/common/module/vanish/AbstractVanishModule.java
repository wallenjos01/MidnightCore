package org.wallentines.midnightcore.common.module.vanish;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.module.vanish.VanishModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public abstract class AbstractVanishModule implements VanishModule {

    protected boolean hideMessages;
    protected Set<MPlayer> globalVanished = new HashSet<>();
    protected HashMap<MPlayer, Set<MPlayer>> vanished = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        reload(section);

        Event.register(SavepointCreatedEvent.class, this, ev -> {

            List<UUID> uuids = new ArrayList<>();
            if(vanished.containsKey(ev.getPlayer())) {
                for (MPlayer mpl : vanished.get(ev.getPlayer())) {
                    uuids.add(mpl.getUUID());
                }
            }

            ev.getSavepoint().getExtraData().set("midnightcore_savepoint", new ConfigSection()
                   .with("global", globalVanished.contains(ev.getPlayer()))
                   .with("vanished", uuids));
        });

        Event.register(SavepointLoadedEvent.class, this, ev -> {

            if(!ev.getSavepoint().getExtraData().has("midnightcore_savepoint")) return;
            ConfigSection sec = ev.getSavepoint().getExtraData().getSection("midnightcore_savepoint");

            if(sec.getBoolean("global")) {
                vanishPlayer(ev.getPlayer());
            }
            for(UUID u : sec.getListFiltered("vanished", UUID.class)) {
                MPlayer mpl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(u);
                vanishPlayerFor(mpl, ev.getPlayer());
            }
        });

        registerEvents();

        return true;
    }

    @Override
    public void reload(ConfigSection config) {

        hideMessages = config.getBoolean("hide_join_messages");

    }

    @Override
    public void vanishPlayer(MPlayer player) {

        if(!globalVanished.contains(player)) {
            for(MPlayer observer : MidnightCoreAPI.getInstance().getPlayerManager()) {
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
            for(MPlayer observer : MidnightCoreAPI.getInstance().getPlayerManager()) {
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

    protected static final ConfigSection DEFAULTS = new ConfigSection().with("hide_join_messages", true);
    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "vanish");

}
