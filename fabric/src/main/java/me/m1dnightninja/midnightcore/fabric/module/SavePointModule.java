package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.AbstractSavePointModule;
import me.m1dnightninja.midnightcore.fabric.api.Location;
import me.m1dnightninja.midnightcore.fabric.api.event.SavePointCreatedEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.SavePointLoadEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;

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

        player.getInventory().clearContent();
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
        out.tag = player.saveWithoutId(new CompoundTag());
        out.gameMode = player.gameMode.getGameModeForPlayer();

        out.extraData = new ConfigSection();

        SavePointCreatedEvent ev = new SavePointCreatedEvent(player, this, out);
        Event.invoke(ev);

        return out;
    }

    @Override
    protected void loadSavePoint(MPlayer u, SavePoint point) {
        ServerPlayer player = ((FabricPlayer) u).getMinecraftPlayer();
        if(player == null) return;

        SavePointLoadEvent ev = new SavePointLoadEvent(player, this, point);
        Event.invoke(ev);
        if(ev.isCancelled() || ev.getSavePoint() == null) return;

        point = ev.getSavePoint();

        resetPlayer(u);
        player.load(point.tag);

        point.location.teleport(player);

        for(MobEffectInstance inst : player.getActiveEffects()) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), inst));
        }

        player.setGameMode(point.gameMode);

    }

    public static class SavePoint {

        Location location;
        CompoundTag tag;
        GameType gameMode;

        ConfigSection extraData;

    }

}
