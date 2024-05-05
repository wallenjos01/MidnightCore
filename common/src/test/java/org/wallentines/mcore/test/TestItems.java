package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public class TestItems {

    @Test
    public void testModern() {

        Common.VERSION.setProtocolVersion(763);

        ItemStack is = ItemStack.Builder.of(new Identifier("minecraft", "grass_block"))
                .withCount(3)
                .withCustomData(new ConfigSection().with("key", "value"))
                .build();

        Assertions.assertEquals(new Identifier("minecraft", "grass_block"), is.getType());
        Assertions.assertEquals(3, is.getCount());
        Assertions.assertNotNull(is.getCustomData());
        Assertions.assertEquals("value", is.getCustomData().getString("key"));


        is = ItemStack.Builder.woolWithColor(TextColor.DARK_GREEN).build();
        Assertions.assertEquals(new Identifier("minecraft", "green_wool"), is.getType());

        is = ItemStack.Builder.glassWithColor(TextColor.RED).build();
        Assertions.assertEquals(new Identifier("minecraft", "red_stained_glass"), is.getType());

        is = ItemStack.Builder.glassPaneWithColor(TextColor.LIGHT_PURPLE).build();
        Assertions.assertEquals(new Identifier("minecraft", "pink_stained_glass_pane"), is.getType());

        is = ItemStack.Builder.concreteWithColor(TextColor.BLACK).build();
        Assertions.assertEquals(new Identifier("minecraft", "black_concrete"), is.getType());

        is = ItemStack.Builder.concretePowderWithColor(TextColor.WHITE).build();
        Assertions.assertEquals(new Identifier("minecraft", "white_concrete_powder"), is.getType());

        is = ItemStack.Builder.terracottaWithColor(TextColor.GREEN).build();
        Assertions.assertEquals(new Identifier("minecraft", "lime_terracotta"), is.getType());

        UUID skinId = UUID.randomUUID();

        int[] splitId = ItemUtil.splitUUID(skinId);
        ConfigList testList = new ConfigList().append(splitId[0]).append(splitId[1]).append(splitId[2]).append(splitId[3]);

        is = ItemStack.Builder.headWithSkin(new Skin(skinId, "DUMMYVALUE==", "DUMMY/SIG-")).build();
        Assertions.assertEquals(new Identifier("minecraft", "player_head"), is.getType());
        Assertions.assertNotNull(is.getCustomData());
        Assertions.assertEquals(4, is.getCustomData().getSection("SkullOwner").getList("Id").size());
        Assertions.assertEquals(testList, is.getCustomData().getSection("SkullOwner").getList("Id"));
        Assertions.assertEquals("DUMMYVALUE==", is.getCustomData().getSection("SkullOwner").getSection("Properties").getList("textures").get(0).asSection().getString("Value"));

    }

    @Test
    public void testLegacy() {

        Common.VERSION.setProtocolVersion(0);

        ItemStack is = ItemStack.Builder.of(new Identifier("minecraft", "grass_block"))
                .withCount(3)
                .withCustomData(new ConfigSection().with("key", "value"))
                .build();

        Assertions.assertEquals(new Identifier("minecraft", "grass_block"), is.getType());
        Assertions.assertEquals(3, is.getCount());
        Assertions.assertNotNull(is.getCustomData());
        Assertions.assertEquals("value", is.getCustomData().getString("key"));


        is = ItemStack.Builder.woolWithColor(TextColor.DARK_GREEN).build();
        Assertions.assertEquals(new Identifier("minecraft", "wool"), is.getType());
        Assertions.assertEquals(13, is.getLegacyDataValue());

        is = ItemStack.Builder.glassWithColor(TextColor.RED).build();
        Assertions.assertEquals(new Identifier("minecraft", "stained_glass"), is.getType());
        Assertions.assertEquals(14, is.getLegacyDataValue());

        is = ItemStack.Builder.glassPaneWithColor(TextColor.LIGHT_PURPLE).build();
        Assertions.assertEquals(new Identifier("minecraft", "stained_glass_pane"), is.getType());
        Assertions.assertEquals(6, is.getLegacyDataValue());

        is = ItemStack.Builder.concreteWithColor(TextColor.BLACK).build();
        Assertions.assertEquals(new Identifier("minecraft", "concrete"), is.getType());
        Assertions.assertEquals(15, is.getLegacyDataValue());

        is = ItemStack.Builder.concretePowderWithColor(TextColor.WHITE).build();
        Assertions.assertEquals(new Identifier("minecraft", "concrete_powder"), is.getType());
        Assertions.assertEquals(0, is.getLegacyDataValue());

        is = ItemStack.Builder.terracottaWithColor(TextColor.GREEN).build();
        Assertions.assertEquals(new Identifier("minecraft", "stained_hardened_clay"), is.getType());
        Assertions.assertEquals(5, is.getLegacyDataValue());

        UUID skinId = UUID.randomUUID();
        is = ItemStack.Builder.headWithSkin(new Skin(skinId, "DUMMYVALUE==", "DUMMY/SIG-")).build();
        Assertions.assertEquals(new Identifier("minecraft", "skull"), is.getType());
        Assertions.assertEquals(3, is.getLegacyDataValue());
        Assertions.assertNotNull(is.getCustomData());
        Assertions.assertEquals(skinId.toString(), is.getCustomData().getSection("SkullOwner").getString("Id"));
        Assertions.assertEquals("DUMMYVALUE==", is.getCustomData().getSection("SkullOwner").getSection("Properties").getList("textures").get(0).asSection().getString("Value"));

    }

    @Test
    public void testItemUtil() {

        UUID uid = UUID.randomUUID();

        int[] split = ItemUtil.splitUUID(uid);
        UUID joined = ItemUtil.joinUUID(split);

        Assertions.assertEquals(uid, joined);

    }

}
