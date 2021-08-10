package me.m1dnightninja.midnightcore.spigot.module.savepoint;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.savepoint.AbstractSavePointModule;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
        pl.setFoodLevel(20);
        pl.setSaturation(20);


        AttributeInstance inst = pl.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double val = inst == null ? 20 : inst.getBaseValue();

        pl.setHealth(val);

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

        out.extraData = new ConfigSection();

        SavePointCreatedEvent ev = new SavePointCreatedEvent(pl, this, out);
        Bukkit.getPluginManager().callEvent(ev);

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

        SavePointLoadEvent ev = new SavePointLoadEvent(pl, this, point);
        Bukkit.getPluginManager().callEvent(ev);
    }

    public static class SavePoint {

        public Location location;
        public Skin skin;
        public HashMap<EquipmentSlot, ItemStack> equipment = new HashMap<>();
        public HashMap<ItemStack, Integer> inventory = new HashMap<>();
        public List<PotionEffect> potionEffects = new ArrayList<>();
        public int fireTicks;

        public ConfigSection extraData;

    }

}
