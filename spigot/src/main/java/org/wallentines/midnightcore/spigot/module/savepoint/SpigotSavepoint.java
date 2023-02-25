package org.wallentines.midnightcore.spigot.module.savepoint;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.spigot.adapter.AdapterManager;
import org.wallentines.midnightcore.spigot.adapter.SpigotAdapter;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

public class SpigotSavepoint extends AbstractSavepoint {

    private Location location;
    private GameMode gameMode;
    private ConfigSection tag;

    protected SpigotSavepoint(Identifier id, EnumSet<SaveFlag> flags) {
        super(id, flags);
    }

    public SpigotSavepoint(Identifier id, Location location, GameMode gameMode, ConfigSection tag, ConfigSection extraData, EnumSet<SaveFlag> flags) {
        super(id, flags);
        this.location = location;
        this.gameMode = gameMode;
        this.tag = tag;
        this.extraData = extraData;
    }

    @Override
    public void load(MPlayer pl) {

        SavepointLoadedEvent event = new SavepointLoadedEvent(this, pl);
        Event.invoke(event);

        if(event.isCancelled()) return;

        SpigotAdapter ad = AdapterManager.getAdapter();
        Player spl = ((SpigotPlayer) pl).getInternal();

        pl.teleport(location);
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

            location = pl.getLocation();
            tag = ad.getTag(spl);
            gameMode = spl.getGameMode();

        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public static final Serializer<SpigotSavepoint> SERIALIZER = ObjectSerializer.create(
            Identifier.serializer(MidnightCoreAPI.DEFAULT_NAMESPACE).entry("id", Savepoint::getId),
            Location.SERIALIZER.<SpigotSavepoint>entry("location", sp -> sp.location).optional(),
            InlineSerializer.of(GameMode::name, GameMode::valueOf).<SpigotSavepoint>entry("gameMode", sp -> sp.gameMode).optional(),
            ConfigSection.SERIALIZER.<SpigotSavepoint>entry("tag", sp -> sp.tag).optional(),
            ConfigSection.SERIALIZER.<SpigotSavepoint>entry("extraData", Savepoint::getExtraData).optional(),
            (id, location, gameMode, tag, extraData) -> {
                Collection<SaveFlag> flags = new ArrayList<>();
                if(location != null) flags.add(SaveFlag.LOCATION);
                if(gameMode != null) flags.add(SaveFlag.GAME_MODE);
                if(tag != null) flags.add(SaveFlag.DATA_TAG);
                // TODO: Advancements

                return new SpigotSavepoint(id, location, gameMode, tag, extraData, EnumSet.copyOf(flags));
            }
    );
}
