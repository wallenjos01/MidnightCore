package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.AbstractSavePointModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.Location;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class SavePointModule extends AbstractSavePointModule<SavePointModule.SavePoint> {

    @Override
    public boolean initialize(ConfigSection section) {
        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }

    @Override
    public void resetPlayer(MPlayer u) {
        ServerPlayer player = ((FabricPlayer) u).getMinecraftPlayer();
        if(player == null) return;

        player.inventory.clearContent();
        player.removeAllEffects();
        player.setRemainingFireTicks(0);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().eat(1, 20);
        player.getFoodData().tick(player);

    }

    @Override
    protected SavePoint createSavePoint(MPlayer u) {
        ServerPlayer player = ((FabricPlayer) u).getMinecraftPlayer();
        if(player == null) return null;

        SavePoint out = new SavePoint();
        out.location = Location.getEntityLocation(player);
        out.skin = MidnightCoreAPI.getInstance().getModule(ISkinModule.class).getSkin(u);
        out.tag = player.saveWithoutId(new CompoundTag());

        return out;
    }

    @Override
    protected void loadSavePoint(MPlayer u, SavePoint point) {
        ServerPlayer player = ((FabricPlayer) u).getMinecraftPlayer();
        if(player == null) return;

        resetPlayer(u);

        player.load(point.tag);

        MidnightCoreAPI.getInstance().getModule(ISkinModule.class).setSkin(u, point.skin);
        MidnightCoreAPI.getInstance().getModule(ISkinModule.class).updateSkin(u);

        point.location.teleport(player);

        for(MobEffectInstance inst : player.getActiveEffects()) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), inst));
        }

    }

    protected static class SavePoint {

        Location location;
        Skin skin;
        CompoundTag tag;

    }

}
