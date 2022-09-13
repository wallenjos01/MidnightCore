package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
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
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.InlineSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;

import java.util.*;

public class GenericAdapter implements SpigotAdapter {

    public static final GenericAdapter INSTANCE = new GenericAdapter();

    public static BaseComponent toBaseComponent(MComponent comp) {

        return ComponentSerializer.parse(comp.toString())[0];
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

        if(tag.has("display", ConfigSection.class)) {
            ConfigSection display = tag.getSection("display");
            if(display.has("Name")) im.setDisplayName(display.get("Name", MComponent.class).toLegacyText());
            if(display.has("Lore", List.class)) {

                List<String> lore = new ArrayList<>();
                for(MComponent s : display.getListFiltered("Lore", MComponent.class)) {
                    lore.add(s.toLegacyText());
                }

                im.setLore(lore);
            }
        }

        if(tag.has("Enchantments", List.class)) {

            for(ConfigSection enchantment : tag.getListFiltered("Enchantments", ConfigSection.class)) {

                Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString(enchantment.getString("id")));
                if(enchant == null) continue;

                im.addEnchant(enchant, enchantment.getInt("lvl"), false);
            }
        }

        if(tag.has("CustomModelData", Number.class)) {
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
                display.set("Lore", lore);
            }

            tag.set("display", display);
        }

        if(im.getEnchants().size() > 0) {

            List<ConfigSection> enchants = new ArrayList<>();
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
        return PlayerTag.SERIALIZER.serialize(new PlayerTag(pl));
    }

    @Override
    public void loadTag(Player pl, ConfigSection tag) {
        PlayerTag.SERIALIZER.deserialize(tag).apply(pl);
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return null;
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

        private static final ConfigSerializer<ItemStack> ITEM_SERIALIZER = ConfigSerializer.create(

            ConfigSerializer.entry(PrimitiveSerializers.STRING, "type", it -> it.getType().name()),
            type -> new ItemStack(Material.valueOf(type))
        );

        private static final InlineSerializer<PotionEffectType> EFFECT_TYPE_SERIALIZER = InlineSerializer.of(PotionEffectType::getName, PotionEffectType::getByName);

        private static final ConfigSerializer<PotionEffect> EFFECT_SERIALIZER = ConfigSerializer.create(
                ConfigSerializer.entry(EFFECT_TYPE_SERIALIZER, "type", PotionEffect::getType),
                ConfigSerializer.entry(PrimitiveSerializers.INT, "duration", PotionEffect::getDuration),
                ConfigSerializer.entry(PrimitiveSerializers.INT, "amplifier", PotionEffect::getAmplifier),
                ConfigSerializer.entry(PrimitiveSerializers.BOOLEAN, "ambient", PotionEffect::isAmbient),
                ConfigSerializer.entry(PrimitiveSerializers.BOOLEAN, "particles", PotionEffect::hasParticles),
                ConfigSerializer.entry(PrimitiveSerializers.BOOLEAN, "icon", PotionEffect::hasIcon),
                PotionEffect::new
        );

        public static final ConfigSerializer<PlayerTag> SERIALIZER = ConfigSerializer.create(
                ConfigSerializer.entry(PrimitiveSerializers.INT, "fire_ticks", pt -> pt.fireTicks),
                ConfigSerializer.listEntry(EFFECT_SERIALIZER, "effects", pt -> pt.effects),
                ConfigSerializer.listEntry(ITEM_SERIALIZER, "items", pt -> pt.inventory),
                ConfigSerializer.listEntry(ITEM_SERIALIZER, "armor", pt -> pt.armor),
                ConfigSerializer.entry(PrimitiveSerializers.DOUBLE, "health", pt -> pt.health),
                ConfigSerializer.entry(PrimitiveSerializers.DOUBLE, "max_health", pt -> pt.maxHealth),
                ConfigSerializer.entry(PrimitiveSerializers.INT, "hunger", pt -> pt.hunger),
                ConfigSerializer.entry(PrimitiveSerializers.FLOAT, "saturation", pt -> pt.saturation),
                ConfigSerializer.entry(PrimitiveSerializers.INT, "exp", pt -> pt.exp),
                ConfigSerializer.entry(PrimitiveSerializers.INT, "levels", pt -> pt.expLevels),
                ConfigSerializer.entry(PrimitiveSerializers.BOOLEAN, "allow_flight", pt -> pt.allowFlight),
                ConfigSerializer.entry(PrimitiveSerializers.BOOLEAN, "flying", pt -> pt.flying),
                PlayerTag::new
        );

    }

}
