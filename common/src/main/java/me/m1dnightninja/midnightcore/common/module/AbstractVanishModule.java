package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IVanishModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

import java.util.*;

public abstract class AbstractVanishModule implements IVanishModule {

    private static final MIdentifier ID = MIdentifier.create("midnightcore", "vanish");

    protected final List<MPlayer> hidden = new ArrayList<>();
    protected final HashMap<MPlayer, List<MPlayer>> hiddenFor = new HashMap<>();

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public void hidePlayer(MPlayer player) {

        if(hidden.contains(player)) return;

        hidden.add(player);
        doHidePlayer(player);

    }

    @Override
    public void hidePlayerFor(MPlayer player, MPlayer other) {

        hiddenFor.compute(other, (k,v) -> {
            if(v == null) {
                v = new ArrayList<>();
            }
            v.add(player);
            return v;
        });

        doHidePlayerFor(player, other);
    }

    @Override
    public void showPlayer(MPlayer player) {

        if(hidden.remove(player)) doShowPlayer(player);
    }

    @Override
    public void showPlayerFor(MPlayer player, MPlayer other) {

        if(!hiddenFor.containsKey(other)) return;
        if(hiddenFor.get(other).remove(player)) doShowPlayerFor(player, other);

    }

    public boolean isHiddenFor(MPlayer player, MPlayer other) {

        return hidden.contains(player) || (hiddenFor.containsKey(other) && hiddenFor.get(other).contains(player));
    }

    protected abstract void doHidePlayer(MPlayer player);
    protected abstract void doHidePlayerFor(MPlayer player, MPlayer other);
    protected abstract void doShowPlayer(MPlayer player);
    protected abstract void doShowPlayerFor(MPlayer player, MPlayer other);

    protected void loadFromConfig(MPlayer player, ConfigSection sec) {

        List<UUID> hiddenPl = new ArrayList<>();
        if(hiddenFor.containsKey(player)) for(MPlayer pl1 : hiddenFor.get(player)) {
            hiddenPl.add(pl1.getUUID());
        }
        sec.set("hidden", hidden.contains(player));
        sec.set("hidden_for", hiddenPl);
    }

    protected void saveToConfig(MPlayer player, ConfigSection sec) {

        if(sec == null) return;

        if(sec.getBoolean("hidden")) hidePlayer(player);
        for(UUID u : sec.getListFiltered("hidden_for", UUID.class)) {
            MPlayer other = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(u);
            hidePlayerFor(player, other);
        }
    }


}
