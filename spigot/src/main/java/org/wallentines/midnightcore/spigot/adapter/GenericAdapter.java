package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.mdcfg.ConfigSection;

import java.util.*;

public class GenericAdapter implements SpigotAdapter {

    public static final GenericAdapter INSTANCE = new GenericAdapter();

    public static BaseComponent toBaseComponent(MComponent comp) {

        return ComponentSerializer.parse(comp.toJSONString())[0];
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public GameProfile getGameProfile(Player pl) {

        return new GameProfile(pl.getUniqueId(), pl.getName());
    }

    @Override
    public void sendMessage(Player pl, MComponent comp) {

        BaseComponent cmp = toBaseComponent(comp);
        pl.spigot().sendMessage(ChatMessageType.SYSTEM, NIL_UUID, cmp);
    }

    @Override
    public void sendActionBar(Player pl, MComponent comp) {

        BaseComponent cmp = toBaseComponent(comp);
        pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, NIL_UUID, cmp);
    }

    @Override
    public void sendTitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        pl.sendTitle(comp.toLegacyText(), null, fadeIn, stay, fadeOut);
    }

    @Override
    public void sendSubtitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        pl.sendTitle(null, comp.toLegacyText(), fadeIn, stay, fadeOut);
    }

    @Override
    public void clearTitles(Player pl) {

        pl.resetTitle();
    }

    @Override
    public void setTag(ItemStack is, ConfigSection tag) {

        ItemMeta im = is.getItemMeta();
        if(im == null) return;

        if(tag.hasSection("display")) {
            ConfigSection display = tag.getSection("display");
            if(display.has("Name")) im.setDisplayName(display.get("Name", MComponent.SERIALIZER).toLegacyText());
            if(display.hasList("Lore")) {

                List<String> lore = new ArrayList<>();
                for(MComponent s : display.getListFiltered("Lore", MComponent.SERIALIZER)) {
                    lore.add(s.toLegacyText());
                }

                im.setLore(lore);
            }
        }

        if(tag.hasList("Enchantments")) {

            for(ConfigSection enchantment : tag.getListFiltered("Enchantments", ConfigSection.SERIALIZER)) {

                Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString(enchantment.getString("id")));
                if(enchant == null) continue;

                im.addEnchant(enchant, enchantment.getInt("lvl"), false);
            }
        }

        if(tag.has("CustomModelData")) {
            im.setCustomModelData(tag.getInt("CustomModelData"));
        }
        is.setItemMeta(im);
    }

    @Override
    public ConfigSection getTag(ItemStack is) {ItemMeta im = is.getItemMeta();

        ConfigSection tag = new ConfigSection();

        if(im == null) return tag;

        if(im.hasDisplayName() || im.hasLore()) {

            ConfigSection display = new ConfigSection();
            if(im.hasDisplayName()) display.set("Name", im.getDisplayName());

            List<String> lore = im.getLore();
            if(lore != null) {
                display.set("Lore", ConfigList.of(lore));
            }

            tag.set("display", display);
        }

        if(im.getEnchants().size() > 0) {

            ConfigList enchants = new ConfigList();
            for(Map.Entry<Enchantment, Integer> ent : im.getEnchants().entrySet()) {
                ConfigSection enchant = new ConfigSection();
                enchant.set("id", ent.getKey().getKey().toString());
                enchant.set("lvl", ent.getValue());
            }

            tag.set("Enchantments", enchants);
        }

        if(im.hasCustomModelData()) {
            tag.set("CustomModelData", im.getCustomModelData());
        }

        return tag;
    }

    @Override
    public boolean hasOpLevel(Player pl, int lvl) {
        return pl.isOp();
    }

    @Override
    public ConfigSection getTag(Player pl) {
        return PlayerTag.SERIALIZER.serialize(ConfigContext.INSTANCE, new PlayerTag(pl)).getOrThrow().asSection();
    }

    @Override
    public void loadTag(Player pl, ConfigSection tag) {
        PlayerTag.SERIALIZER.deserialize(ConfigContext.INSTANCE, tag).getOrThrow().apply(pl);
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return null;
    }

    @Override
    public ItemStack setupInternal(ItemStack item) {
        return item;
    }

    @Override
    public void addTickable(Runnable runnable) {
        Bukkit.getScheduler().runTaskTimer(MidnightCore.getInstance(), runnable, 50L, 50L);
    }

    private static class PlayerTag {

        private final int fireTicks;
        private final List<PotionEffect> effects;
        private final List<ItemStack> inventory;
        private final List<ItemStack> armor;
        private final double health;
        private final double maxHealth;
        private final int hunger;
        private final float saturation;
        private final int exp;
        private final int expLevels;
        private final boolean allowFlight;
        private final boolean flying;

        public PlayerTag(int fireTicks, Collection<PotionEffect> effects, Collection<ItemStack> inventory, Collection<ItemStack> armor, double health, double maxHealth, int hunger, float saturation, int exp, int expLevels, boolean allowFlight, boolean flying) {
            this.fireTicks = fireTicks;
            this.effects = new ArrayList<>(effects);
            this.inventory = new ArrayList<>(inventory);
            this.armor = new ArrayList<>(armor);
            this.health = health;
            this.maxHealth = maxHealth;
            this.hunger = hunger;
            this.saturation = saturation;
            this.exp = exp;
            this.expLevels = expLevels;
            this.allowFlight = allowFlight;
            this.flying = flying;
        }

        public PlayerTag(Player player) {
            this.fireTicks = player.getFireTicks();
            this.effects = new ArrayList<>(player.getActivePotionEffects());
            this.inventory = Arrays.asList(player.getInventory().getContents());
            this.armor = Arrays.asList(player.getInventory().getArmorContents());
            this.health = player.getHealth();

            AttributeInstance inst = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            this.maxHealth = inst == null ? 20.0 : inst.getBaseValue();

            this.hunger = player.getFoodLevel();
            this.saturation = player.getSaturation();
            this.exp = player.getTotalExperience();
            this.expLevels = player.getLevel();
            this.allowFlight = player.getAllowFlight();
            this.flying = player.isFlying();
        }

        public void apply(Player player) {

            player.setFireTicks(fireTicks);
            for(PotionEffect eff : effects) {
                player.addPotionEffect(eff);
            }
            player.getInventory().clear();
            for(ItemStack is : inventory) {
                player.getInventory().addItem(is);
            }
            for(int i = 0 ; i < 4 ; i++) {
                player.getInventory().getArmorContents()[i] = armor.get(i);
            }
            player.setHealth(health);

            AttributeInstance inst = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if(inst != null) inst.setBaseValue(maxHealth);

            player.setFoodLevel(hunger);
            player.setSaturation(saturation);
            player.setTotalExperience(exp);
            player.setLevel(expLevels);
            player.setAllowFlight(allowFlight);
            player.setFlying(flying);
        }

        private static final Serializer<ItemStack> ITEM_SERIALIZER = ObjectSerializer.create(
            InlineSerializer.of(Material::name, Material::valueOf).entry("type", ItemStack::getType),
            Serializer.INT.entry("Count", ItemStack::getAmount),
            ItemStack::new
        );

        private static final InlineSerializer<PotionEffectType> EFFECT_TYPE_SERIALIZER = InlineSerializer.of(PotionEffectType::getName, PotionEffectType::getByName);

        private static final Serializer<PotionEffect> EFFECT_SERIALIZER = ObjectSerializer.create(
                EFFECT_TYPE_SERIALIZER.entry("type", PotionEffect::getType),
                Serializer.INT.entry("duration", PotionEffect::getDuration),
                Serializer.INT.entry("amplifier", PotionEffect::getAmplifier),
                Serializer.BOOLEAN.entry("ambient", PotionEffect::isAmbient),
                Serializer.BOOLEAN.entry("particles", PotionEffect::hasParticles),
                Serializer.BOOLEAN.entry("icon", PotionEffect::hasIcon),
                PotionEffect::new
        );

        public static final Serializer<PlayerTag> SERIALIZER = ObjectSerializer.create(
                Serializer.INT.entry("fire_ticks", pt -> pt.fireTicks),
                EFFECT_SERIALIZER.listOf().entry("effects", pt -> pt.effects),
                ITEM_SERIALIZER.listOf().entry("items", pt -> pt.inventory),
                ITEM_SERIALIZER.listOf().entry("armor", pt -> pt.armor),
                Serializer.DOUBLE.entry("health", pt -> pt.health),
                Serializer.DOUBLE.entry("max_health", pt -> pt.maxHealth),
                Serializer.INT.entry("hunger", pt -> pt.hunger),
                Serializer.FLOAT.entry("saturation", pt -> pt.saturation),
                Serializer.INT.entry("exp", pt -> pt.exp),
                Serializer.INT.entry("levels", pt -> pt.expLevels),
                Serializer.BOOLEAN.entry("allow_flight", pt -> pt.allowFlight),
                Serializer.BOOLEAN.entry("flying", pt -> pt.flying),
                PlayerTag::new
        );

    }

}
