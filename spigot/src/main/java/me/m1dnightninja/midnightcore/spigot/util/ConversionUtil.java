package me.m1dnightninja.midnightcore.spigot.util;

import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ConversionUtil {

    public static ItemStack toBukkitStack(MItemStack is) {

        Material mat = Material.getMaterial(is.getType().toString());
        if(mat == null) return null;

        ItemStack out = new ItemStack(mat, is.getCount());

        out.getItemMeta();

        return out;
    }

    public static TextComponent toSpigotComponent(MComponent comp) {

        TextComponent out = new TextComponent(comp.getContent());
        out.setBold(comp.getStyle().isBold());
        out.setItalic(comp.getStyle().isBold());
        out.setUnderlined(comp.getStyle().isBold());
        out.setStrikethrough(comp.getStyle().isBold());
        out.setObfuscated(comp.getStyle().isBold());
        out.setColor(ChatColor.getByChar(Integer.toHexString(comp.getStyle().getColor().toRGBI()).charAt(0)));
        out.setFont(comp.getStyle().getFont().toString());



        for(MComponent child : comp.getChildren()) {
            out.addExtra(toSpigotComponent(child));
        }

        return out;
    }

}
