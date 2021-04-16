package me.m1dnightninja.midnightcore.fabric.util;

import me.m1dnightninja.midnightcore.api.Color;
import me.m1dnightninja.midnightcore.api.skin.Skin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemBuilder {

    private final Item type;

    private int amount = 1;

    private Skin headSkin = null;
    private Component name = null;
    private Iterable<Component> lore = null;

    public static final Style BASE_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(Boolean.FALSE);

    private ItemBuilder(Item type) {
        this.type = type;
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder withName(Component name) {
        this.name = name;
        return this;
    }

    public ItemBuilder withLore(Iterable<Component> lore) {
        this.lore = lore;
        return this;
    }

    public ItemStack build() {

        ItemStack is = new ItemStack(type, amount);
        CompoundTag tag = is.getOrCreateTag();

        if(name != null || lore != null) {
            CompoundTag display = new CompoundTag();

            if(name != null) {

                display.put("Name", StringTag.valueOf(Component.Serializer.toJson(name)));
            }

            if(lore != null) {

                ListTag listLore = new ListTag();
                for(Component cmp : lore) {
                    listLore.add(StringTag.valueOf(Component.Serializer.toJson(cmp)));
                }

                display.put("Lore", listLore);
            }

            tag.put("display", display);
        }

        if(headSkin != null) {

            CompoundTag skullOwner = new CompoundTag();
            skullOwner.putUUID("Id", headSkin.getUUID());

            CompoundTag properties = new CompoundTag();
            ListTag textures = new ListTag();

            CompoundTag property = new CompoundTag();
            property.putString("Value", headSkin.getBase64());

            textures.add(property);
            properties.put("textures", textures);

            tag.put("SkullOwner", skullOwner);
            tag.put("Properties", properties);

        }

        return is;

    }


    public static ItemBuilder of(Item type) {
        return new ItemBuilder(type);
    }

    public static ItemBuilder woolWithColor(Color color) {

        String colorName = color.toDyeColor();

        Item type = Registry.ITEM.get(new ResourceLocation(colorName + "_wool"));
        return new ItemBuilder(type);

    }

    public static ItemBuilder headWithSkin(Skin skin) {

        ItemBuilder out = new ItemBuilder(Items.PLAYER_HEAD);
        out.headSkin = skin;

        return out;

    }

}
