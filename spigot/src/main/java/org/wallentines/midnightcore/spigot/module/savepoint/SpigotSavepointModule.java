package org.wallentines.midnightcore.spigot.module.savepoint;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepointModule;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class SpigotSavepointModule extends AbstractSavepointModule {

    @Override
    public void resetPlayer(MPlayer pl) {

        Player spl = ((SpigotPlayer) pl).getInternal();
        if(spl == null) return;

        spl.getInventory().clear();
        for(PotionEffect eff : spl.getActivePotionEffects()) {
            spl.removePotionEffect(eff.getType());
        }
        spl.setFireTicks(0);

        AttributeInstance att = spl.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = att == null ? 20.0d : att.getBaseValue();

        spl.setHealth(maxHealth);
        spl.setFoodLevel(20);
        spl.setSaturation(5.0f);

        spl.updateInventory();
    }

    @Override
    public Savepoint createSavepoint(Identifier id) {

        return new SpigotSavepoint(id);
    }

    @Override
    public Serializer<Savepoint> getSerializer() {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, Savepoint value) {
                if(!(value instanceof SpigotSavepoint)) return SerializeResult.failure(value + " is not a Spigot Savepoint!");
                return SpigotSavepoint.SERIALIZER.serialize(context, (SpigotSavepoint) value).flatMap(sp -> sp);
            }

            @Override
            public <O> SerializeResult<Savepoint> deserialize(SerializeContext<O> context, O value) {
                return SpigotSavepoint.SERIALIZER.deserialize(context, value).flatMap(sp -> sp);
            }
        };
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotSavepointModule::new, ID, DEFAULT_CONFIG);

}
