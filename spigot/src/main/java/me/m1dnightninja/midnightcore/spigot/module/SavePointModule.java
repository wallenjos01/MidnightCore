package me.m1dnightninja.midnightcore.spigot.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.AbstractSavePointModule;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class SavePointModule extends AbstractSavePointModule<SavePointModule.SavePoint> {

    @Override
    public boolean initialize(ConfigSection configuration) {
        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public void resetPlayer(MPlayer u) {

        Player pl = ((SpigotPlayer) u).getSpigotPlayer();
        if(pl == null) return;

        pl.resetTitle();
        pl.getInventory().clear();

        for(PotionEffect type : pl.getActivePotionEffects()) {
            pl.removePotionEffect(type.getType());
        }

        pl.setFireTicks(0);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        pl.setFoodLevel(20);
        pl.setSaturation(20);

    }

    @Override
    protected SavePoint createSavePoint(MPlayer u) {

        Player pl = ((SpigotPlayer) u).getSpigotPlayer();
        if(pl == null) return null;

        SavePoint out = new SavePoint();
        out.location = pl.getLocation();
        out.skin = u.getSkin();

        out.equipment.put(EquipmentSlot.HEAD,     pl.getInventory().getHelmet());
        out.equipment.put(EquipmentSlot.CHEST,    pl.getInventory().getChestplate());
        out.equipment.put(EquipmentSlot.LEGS,     pl.getInventory().getLeggings());
        out.equipment.put(EquipmentSlot.FEET,     pl.getInventory().getBoots());
        out.equipment.put(EquipmentSlot.OFF_HAND, pl.getInventory().getItemInOffHand());

        int i = 0;
        for(ItemStack is : pl.getInventory().getContents()) {
            i++;
            if(is == null || is.getType() == Material.AIR) continue;
            out.inventory.put(is, i);
        }
        out.potionEffects.addAll(pl.getActivePotionEffects());
        out.fireTicks = pl.getFireTicks();

        return out;
    }

    @Override
    protected void loadSavePoint(MPlayer u, SavePoint point) {

        Player pl = ((SpigotPlayer) u).getSpigotPlayer();
        if(pl == null) return;

        for(PotionEffect eff : point.potionEffects) {
            pl.addPotionEffect(eff);
        }

        for(Map.Entry<ItemStack, Integer> ent : point.inventory.entrySet()) {
            pl.getInventory().setItem(ent.getValue(), ent.getKey());
        }

        for(Map.Entry<EquipmentSlot, ItemStack> ent : point.equipment.entrySet()) {
            if(ent.getValue() == null || ent.getValue().getType() == Material.AIR) return;
            pl.getInventory().setItem(ent.getKey(), ent.getValue());
        }

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);

        if(mod != null) {
            mod.setSkin(u, point.skin);
            mod.updateSkin(u);
        }

        pl.teleport(point.location);
        pl.setFireTicks(point.fireTicks);
    }

    protected static class SavePoint {

        Location location;
        Skin skin;
        HashMap<EquipmentSlot, ItemStack> equipment = new HashMap<>();
        HashMap<ItemStack, Integer> inventory = new HashMap<>();
        List<PotionEffect> potionEffects = new ArrayList<>();
        int fireTicks;

    }

}
