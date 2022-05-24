package org.wallentines.midnightcore.spigot.module.savepoint;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.spigot.adapter.AdapterManager;
import org.wallentines.midnightcore.spigot.adapter.SpigotAdapter;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightcore.spigot.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

public class SpigotSavepoint extends AbstractSavepoint {

    private Location loc;
    private GameMode gameMode;
    private ConfigSection tag;

    protected SpigotSavepoint(Identifier id) {
        super(id);
    }

    @Override
    public void load(MPlayer pl) {

        SavepointLoadedEvent event = new SavepointLoadedEvent(this, pl);
        Event.invoke(event);

        if(event.isCancelled()) return;

        SpigotAdapter ad = AdapterManager.getAdapter();
        Player spl = ((SpigotPlayer) pl).getInternal();

        spl.teleport(loc);
        for(PotionEffect pe : spl.getActivePotionEffects()) {
            spl.removePotionEffect(pe.getType());
        }

        ad.loadTag(spl, tag);
        spl.setGameMode(gameMode);
    }

    @Override
    public boolean save(MPlayer pl) {

        SavepointCreatedEvent event = new SavepointCreatedEvent(this, pl);
        Event.invoke(event);

        if(event.isCancelled()) return false;

        try {

            SpigotAdapter ad = AdapterManager.getAdapter();
            Player spl = ((SpigotPlayer) pl).getInternal();

            loc = spl.getLocation();
            tag = ad.getTag(spl);
            gameMode = spl.getGameMode();

        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    @Override
    public void deserialize(ConfigSection section) {

        loc = ConversionUtil.toBukkitLocation(section.get("location", org.wallentines.midnightcore.api.player.Location.class));
        gameMode = GameMode.valueOf(section.getString("gameMode"));
        tag = section.getSection("tag");
        extraData = section.getSection("extraData");
    }

    @Override
    public ConfigSection serialize() {

        return new ConfigSection()
            .with("location", ConversionUtil.toLocation(loc))
            .with("gameMode", gameMode.name())
            .with("tag", tag)
            .with("extraData", extraData);
    }
}
