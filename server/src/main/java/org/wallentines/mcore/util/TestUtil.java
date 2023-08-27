package org.wallentines.mcore.util;

import org.wallentines.mcore.*;
import org.wallentines.mcore.item.InventoryGUI;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.item.UnresolvedItemStack;
import org.wallentines.mcore.lang.*;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.text.*;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

public class TestUtil {

    public static void cmd(Player pl) {

        try {
            LangRegistry defaults = new LangRegistry();
            defaults.register("test", UnresolvedComponent.parse("default", PlaceholderManager.INSTANCE).getOrThrow());

            LangManager manager = new LangManager(defaults, null);

            LangRegistry english = new LangRegistry();
            english.register("test", UnresolvedComponent.parse("&cEnglish, %player_name%", PlaceholderManager.INSTANCE).getOrThrow());

            LangRegistry spanish = new LangRegistry();
            spanish.register("test", UnresolvedComponent.parse("&aSpanish, %player_name%", PlaceholderManager.INSTANCE).getOrThrow());

            manager.setLanguageEntries("en_us", english);
            manager.setLanguageEntries("es_mx", spanish);


            pl.sendMessage(org.wallentines.mcore.text.Component.text("Hello").withColor(TextColor.RED));
            pl.sendMessage(LangContent.component(manager, "test"));

            ItemStack is = ItemStack.Builder.woolWithColor(TextColor.RED)
                    .withCount(13)
                    .withName(org.wallentines.mcore.text.Component.text("Test").withColor(TextColor.GREEN))
                    .withLore(Arrays.asList(org.wallentines.mcore.text.Component.text("Bruh")))
                    .build();

            Server srv = Server.RUNNING_SERVER.get();

            pl.sendMessage(
                    Component.text(GameVersion.CURRENT_VERSION.get().getId() + " (" + GameVersion.CURRENT_VERSION.get().getProtocolVersion() + ") [" + srv.isDedicatedServer() + "]")
                            .withHoverEvent(HoverEvent.createItemHover(is))
            );

            srv.submit(() -> {
                pl.sendMessage(
                        Component.text("Submitted")
                                .withHoverEvent(HoverEvent.createTextHover(Component.text("Hello").withColor(TextColor.GOLD)))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s ~ ~5 ~"))
                );
            });

            // Cursed hack to convert between spigot and vanilla dimension naming
            String dim = "world_nether";
            try {
                Class.forName("org.wallentines.mcore.SpigotServer");
            } catch (ClassNotFoundException ex) {
                dim = "the_nether";
            }

            pl.teleport(new Location(new Identifier("minecraft", dim), 0, 100, 0, 0, 0));
            pl.giveItem(is);

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    private static final UUID SKIN_UUID = UUID.fromString("ce784258-10ca-45fb-b787-8dde07375f2b");

    public static void skinCmd(Player pl) {

        try {

            pl.sendMessage(org.wallentines.mcore.text.Component.text("Attempting to download skin...").withColor(TextColor.AQUA));
            MojangUtil.getSkinAsync(SKIN_UUID).thenAccept(skin -> {
                pl.sendMessage(org.wallentines.mcore.text.Component.text("Skin obtained!").withColor(TextColor.AQUA));
                pl.setSkin(skin);
            });

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void saveCmd(Player pl) {
        try {
            pl.getServer().getModuleManager().getModule(SavepointModule.class).savePlayer(pl, "test", EnumSet.allOf(SavepointModule.SaveFlag.class));
            pl.sendMessage(org.wallentines.mcore.text.Component.text("Saved!").withColor(TextColor.AQUA));
        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void loadCmd(Player pl) {
        try {
            pl.getServer().getModuleManager().getModule(SavepointModule.class).loadPlayer(pl, "test");
            pl.sendMessage(org.wallentines.mcore.text.Component.text("Loaded!").withColor(TextColor.AQUA));
        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void guiCmd(Player pl) {

        try {

            InventoryGUI gui = InventoryGUI.FACTORY.get().build(Component.text("Hello"), 3);

            gui.setItem(0, ItemStack.Builder.concreteWithColor(TextColor.RED).withName(Component.text("Hello, World").withColor(TextColor.AQUA)).build(), null);
            gui.setItem(3, new UnresolvedItemStack(ItemStack.Builder.concreteWithColor(TextColor.GREEN), PlaceholderContent.component("%player_name%"), null), (cpl, cl) -> {
                cpl.sendMessage(Component.text(cl.name()));
                gui.close(cpl);
            });gui.setItem(8, ItemStack.empty(), (cpl, cl) -> {
                cpl.sendMessage(Component.text("Hello, " + cpl.getUsername()));
            });

            gui.open(pl);
        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void scoreboardCmd(Player pl) {
        try {

            CustomScoreboard board = CustomScoreboard.FACTORY.get().create(Component.text("Test").withColor(TextColor.RED).withBold(true));

            board.setLine(13, Component.text("Hello"));
            board.setLine(1, PlaceholderContent.component("%player_name%"));
            board.setLine(0, Component.text("Zero").withColor(TextColor.AQUA).withItalic(true));

            board.addViewer(pl);

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void equipCmd(Player pl) {
        try {

            ItemStack hat = ItemStack.Builder
                    .of(new Identifier("minecraft", "turtle_helmet"))
                    .withEnchantment(new Identifier("minecraft", "respiration"), 3)
                    .build();

            ItemStack shirt = ItemStack.Builder
                    .of(new Identifier("minecraft", "iron_chestplate"))
                    .withEnchantment(new Identifier("minecraft", "protection"), 5)
                    .build();

            ItemStack legs = ItemStack.Builder
                    .of(new Identifier("minecraft", "golden_leggings"))
                    .withEnchantment(new Identifier("minecraft", "unbreaking"), 3)
                    .build();

            ItemStack feet = ItemStack.Builder
                    .of(new Identifier("minecraft", "netherite_boots"))
                    .withEnchantment(new Identifier("minecraft", "feather_falling"), 4)
                    .build();

            ItemStack cmd = ItemStack.Builder
                    .of(new Identifier("minecraft", "command_block"))
                    .withName(Component.text("Hello").withColor(new Color(0x398F3C)))
                    .build();

            ItemStack sword = ItemStack.Builder
                    .of(new Identifier("minecraft", "netherite_sword"))
                    .withEnchantment(new Identifier("minecraft", "sharpness"), 100)
                    .withName(Component.text("Test Sword").withColor(new Color(0xAF4EBE)))
                    .build();

            pl.setItem(Entity.EquipmentSlot.HEAD, hat);
            pl.setItem(Entity.EquipmentSlot.CHEST, shirt);
            pl.setItem(Entity.EquipmentSlot.LEGS, legs);
            pl.setItem(Entity.EquipmentSlot.FEET, feet);
            pl.setItem(Entity.EquipmentSlot.OFFHAND, cmd);
            pl.setItem(Entity.EquipmentSlot.MAINHAND, sword);

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

}
