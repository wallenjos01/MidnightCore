package me.m1dnightninja.midnightcore.spigot.util;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class ConversionUtil {

    public static ItemStack toBukkitStack(MItemStack is) {

        Material mat = Material.matchMaterial(is.getType().toString());

        if(mat == null) {
            MidnightCoreAPI.getLogger().warn("Unable to find material for " + is.getType().getPath().toUpperCase(Locale.ROOT));
            return null;
        }

        ItemStack out = new ItemStack(mat, is.getCount());
        return NMSWrapper.setItemTag(out, is.getTag());
    }

    public static MIdentifier fromNamespacedKey(NamespacedKey key) {
        return MIdentifier.create(key.getNamespace(), key.getKey());
    }

    public static TextComponent toSpigotComponent(MComponent comp) {

        TextComponent out = new TextComponent(comp.getContent());
        out.setBold(comp.getStyle().isBold());
        out.setItalic(comp.getStyle().isItalic());
        out.setUnderlined(comp.getStyle().isUnderlined());
        out.setStrikethrough(comp.getStyle().isStrikethrough());
        out.setObfuscated(comp.getStyle().isObfuscated());

        if(comp.getStyle().getColor() != null) {
            out.setColor(ChatColor.getByChar(Integer.toHexString(comp.getStyle().getColor().toRGBI()).charAt(0)));
        }

        if(ReflectionUtil.MAJOR_VERISON >= 16 && comp.getStyle().getFont() != null) {
            out.setFont(comp.getStyle().getFont().toString());
        }

        for(MComponent child : comp.getChildren()) {
            out.addExtra(toSpigotComponent(child));
        }

        return out;
    }
}
