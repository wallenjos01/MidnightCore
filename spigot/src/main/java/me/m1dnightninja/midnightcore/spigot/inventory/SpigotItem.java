package me.m1dnightninja.midnightcore.spigot.inventory;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import me.m1dnightninja.midnightcore.spigot.util.LegacyUtil;
import me.m1dnightninja.midnightcore.spigot.util.NMSUtil;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class SpigotItem extends MItemStack {

    ItemStack stack;

    public SpigotItem(MIdentifier type, int count) {
        super(type, count);

        stack = toBukkitStack();
    }

    public SpigotItem(MIdentifier type, int count, ConfigSection tag) {
        super(type, count, tag);

        stack = toBukkitStack();
    }

    @SuppressWarnings("deprecation")
    public SpigotItem(ItemStack is) {
        super(MidnightCoreAPI.getInstance().getGameMajorVersion() >= 13 ?
                ConversionUtil.fromNamespacedKey(is.getType().getKey()) :
                MIdentifier.create("minecraft", is.getType().name()),
                is.getAmount(), NMSUtil.getItemTag(is));
        stack = is;

        if(MidnightCoreAPI.getInstance().getGameMajorVersion() < 13 && is.getData() != null) {
            data = is.getData().getData();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void update() {

        if(stack == null) return;
        if(MidnightCoreAPI.getInstance().getGameMajorVersion() < 13 && stack.getData() != null && data != stack.getData().getData()) {
            stack = toBukkitStack();
        }
        if(stack == null) return;

        NMSUtil.setItemTag(stack, tag);
        stack.setAmount(count);

    }

    @Override
    public MComponent getName() {

        ItemMeta im = stack.getItemMeta();
        if(im != null && im.hasDisplayName()) {
            return MComponent.Serializer.parse(im.getDisplayName());
        }

        if(MidnightCoreAPI.getInstance().getGameMajorVersion() >= 13) {

            NamespacedKey key = stack.getType().getKey();
            return MComponent.createTranslatableComponent((stack.getType().isBlock() ? "block" : "item") + "." + key.getNamespace() + "." + key.getKey());

        } else {

            String name = stack.getType().name().toLowerCase(Locale.ROOT);
            StringBuilder camelCase = new StringBuilder();
            for(int i = 0 ; i < name.length() ; i++) {
                char c = name.charAt(i);
                if(c == '_' && i + 1 < name.length()) {
                    i++;
                    c = name.charAt(i);
                    if(c >= 'a' && c <= 'z') {
                        camelCase.append(c - 32);
                        continue;
                    }
                }
                camelCase.append(c);
            }

            return MComponent.createTranslatableComponent((stack.getType().isBlock() ? "tile" : "item") + "." + camelCase + ".name");

        }


    }

    public ItemStack getBukkitStack() {
        return stack;
    }

    @SuppressWarnings("deprecation")
    private ItemStack toBukkitStack() {

        ItemStack out;

        if(ReflectionUtil.MAJOR_VERISON >= 13) {
            Material mat = Material.matchMaterial(getType().toString());

            if(mat == null) {
                MidnightCoreAPI.getLogger().warn("Unable to find material for " + getType().getPath().toUpperCase(Locale.ENGLISH));
                return null;
            }

            out = new ItemStack(mat, getCount());

        } else {

            out = LegacyUtil.fromLegacyMaterial(getType(), data);
            if(out == null) {

                MidnightCoreAPI.getLogger().warn("Unable to find material for " + getType().getPath().toUpperCase(Locale.ENGLISH));
                return null;
            }

            if(out.getData() != null) data = out.getData().getData();
            out.setAmount(getCount());
        }

        return NMSUtil.setItemTag(out, getTag());
    }

}
