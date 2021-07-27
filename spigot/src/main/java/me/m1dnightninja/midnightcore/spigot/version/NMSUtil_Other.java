package me.m1dnightninja.midnightcore.spigot.version;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NMSUtil_Other implements NMSWrapper.NMSUtil {

    public static final UUID nullUid = new UUID(0L, 0L);

    public GameProfile getGameProfile(Player player) {

        return new GameProfile(player.getUniqueId(), player.getName());
    }

    public void sendMessage(Player player, MComponent comp) {

        TextComponent cmp = new TextComponent(ComponentSerializer.parse(MComponent.Serializer.toJsonString(comp)));
        player.spigot().sendMessage(ChatMessageType.SYSTEM, nullUid, cmp);
    }

    @Override
    public void sendActionBar(Player pl, ActionBar ab) {

        TextComponent comp = new TextComponent(ComponentSerializer.parse(MComponent.Serializer.toJsonString(ab.getText())));
        pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, nullUid, comp);
    }

    @Override
    public void sendTitle(Player pl, Title title) {

        if(title.getOptions().clear) {
            pl.resetTitle();
        }

        String ttl = title.getText().toLegacyText(false);
        if(title.getOptions().subtitle) {
            pl.sendTitle(ttl, null, 20, 80, 20);
        } else {
            pl.sendTitle(null, ttl, 20, 80, 20);
        }

    }

    @Override
    public ConfigSection getItemTag(ItemStack is) {

        ItemMeta im = is.getItemMeta();
        ConfigSection tag = new ConfigSection();

        if(im == null) return tag;

        if(im.hasDisplayName() || im.hasLore()) {

            ConfigSection display = new ConfigSection();
            if(im.hasDisplayName()) display.set("Name", MComponent.Serializer.toJsonString(MComponent.Serializer.parseLegacyText(im.getDisplayName(), '§', null)));

            List<String> iLore = im.getLore();
            if(iLore != null) {
                List<String> lore = new ArrayList<>();
                for(String s : iLore) {
                    lore.add(MComponent.Serializer.toJsonString(MComponent.Serializer.parseLegacyText(s, '§', null)));
                }
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
    public ItemStack setItemTag(ItemStack is, ConfigSection tag) {

        ItemMeta im = is.getItemMeta();
        if(im == null) return is;

        if(tag.has("display", ConfigSection.class)) {
            ConfigSection display = tag.getSection("display");
            if(display.has("Name")) im.setDisplayName(MComponent.Serializer.fromJson(display.getString("Name")).toLegacyText(false));
            if(display.has("Lore", List.class)) {

                List<String> lore = new ArrayList<>();
                for(String s : display.getListFiltered("Lore", String.class)) {
                    lore.add(MComponent.Serializer.fromJson(s).toLegacyText(false));
                }

                im.setLore(lore);
            }
        }

        if(tag.has("Enchantments", List.class)) {

            for(ConfigSection ench : tag.getListFiltered("Enchantments", ConfigSection.class)) {

                Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString(ench.getString("id")));
                if(enchant == null) continue;

                im.addEnchant(enchant, ench.getInt("lvl"), false);
            }
        }

        if(tag.has("CustomModelData", Number.class)) {
            im.setCustomModelData(tag.getInt("CustomModelData"));
        }
        is.setItemMeta(im);

        return is;
    }

}
