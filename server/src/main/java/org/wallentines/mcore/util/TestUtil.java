package org.wallentines.mcore.util;

import org.wallentines.mcore.*;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.messenger.Messenger;
import org.wallentines.mcore.messenger.ServerMessengerModule;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.text.ClickEvent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.HoverEvent;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

public class TestUtil {

    public static void cmd(Player pl) {

        try {
            LangRegistry defaults = new LangRegistry();
            defaults.register("test", UnresolvedComponent.parse("default").getOrThrow());

            LangManager manager = new LangManager(defaults, null);

            LangRegistry english = new LangRegistry();
            english.register("test", UnresolvedComponent.parse("&cEnglish, %player_name%").getOrThrow());

            LangRegistry spanish = new LangRegistry();
            spanish.register("test", UnresolvedComponent.parse("&aSpanish, %player_name%").getOrThrow());

            manager.setLanguageEntries("en_us", english);
            manager.setLanguageEntries("es_mx", spanish);

            pl.sendMessage(org.wallentines.mcore.text.Component.text("\u00BB Hello").withColor(TextColor.RED));
            pl.sendMessage(manager.component("test"));

            Server srv = pl.getServer();
            GameVersion version = srv.getVersion();

            ItemStack is = ItemStack.Builder.woolWithColor(version, TextColor.RED)
                    .withCount(13)
                    .withName(org.wallentines.mcore.text.Component.text("Test").withColor(TextColor.GREEN))
                    .withLore(Arrays.asList(org.wallentines.mcore.text.Component.text("Bruh")))
                    .build();


            pl.sendMessage(
                    Component.text(version.getId() + " (" + version.getProtocolVersion() + ") [" + srv.isDedicatedServer() + "]")
                            .withHoverEvent(HoverEvent.forItem(is))
            );

            ItemStack hand = pl.getItem(Entity.EquipmentSlot.MAINHAND);
            pl.sendMessage(
                    hand.getName().addChild(Component.text(" (" + hand.getTranslationKey() + ")"))
            );

            srv.submit(() -> {
                pl.sendMessage(
                        Component.text("Submitted")
                                .withHoverEvent(HoverEvent.create(Component.text("Hello").withColor(TextColor.GOLD)))
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

            GameVersion version = pl.getServer().getVersion();
            InventoryGUI gui = InventoryGUI.create(Component.text("Hello"), 3);

            gui.setItem(0, ItemStack.Builder.woolWithColor(version, TextColor.RED).withName(Component.text("Hello, World").withColor(TextColor.AQUA)).build(), null);
            gui.setItem(3, new UnresolvedItemStack(ItemStack.Builder.glassWithColor(version, TextColor.GREEN), UnresolvedComponent.parse("%player_name%").getOrThrow(), null), (cpl, cl) -> {
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

    public static void pagedGuiCmd(Player pl) {

        try {

            UnresolvedItemStack next = new UnresolvedItemStack(ItemStack.Builder.glassPaneWithColor(TextColor.GREEN), Component.text("Next Page"));
            UnresolvedItemStack prev = new UnresolvedItemStack(ItemStack.Builder.glassPaneWithColor(TextColor.RED), Component.text("Previous Page"));

            GameVersion version = pl.getServer().getVersion();
            PagedInventoryGUI gui = InventoryGUI.createPaged(UnresolvedComponent.parse("Paged - %gui_page%/%gui_pages%").getOrThrow(), PagedInventoryGUI.SizeProvider.dynamic(5), 256);
            gui.addBottomReservedRow(PagedInventoryGUI.RowProvider.pageControls(next, prev));

            Color[] cs = new Color[]{ TextColor.RED, TextColor.YELLOW, TextColor.GREEN, TextColor.BLUE };
            for(int c = 0 ; c < cs.length; c++) {
                for(int i = 0 ; i < 64 ; i++) {
                    ItemStack is = ItemStack.Builder.woolWithColor(version, cs[c]).withCount(i + 1).build();
                    int realIndex = c * 64 + i;
                    gui.setItem(realIndex, is, (cpl, ct, page) -> {
                        cpl.sendMessage(Component.text("Page " + (page + 1) + ", Item " + (realIndex + 1)));
                    });
                }
            }

            gui.open(pl, 0);
        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void scoreboardCmd(Player pl) {
        try {

            CustomScoreboard board = CustomScoreboard.create(Component.text("Test").withColor(TextColor.RED).withBold(true));

            board.setLine(13, Component.text("Hello"));
            board.setNumberFormat(13, CustomScoreboard.NumberFormatType.BLANK);
            board.setLine(3, PlaceholderManager.INSTANCE.parse("%player_name%"), PlaceholderManager.INSTANCE.parse("%player_username%"));
            board.setLine(1, Component.text("Color").withColor(new Color(0x58BE44)));
            board.setNumberFormat(1, CustomScoreboard.NumberFormatType.STYLED, Component.empty().withColor(TextColor.GREEN).withBold(true));
            board.setLine(0, Component.text("Zero").withColor(TextColor.AQUA).withItalic(true));

            board.addViewer(pl);

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void equipCmd(Player pl) {
        try {

            GameVersion version = pl.getServer().getVersion();
            ItemStack hat = ItemStack.Builder
                    .of(version, new Identifier("minecraft", "leather_helmet"))
                    .withEnchantment(new Identifier("minecraft", "respiration"), 3)
                    .withLegacyEnchantment(5, 3)
                    .build();

            ItemStack shirt = ItemStack.Builder
                    .of(version, new Identifier("minecraft", "iron_chestplate"))
                    .withEnchantment(new Identifier("minecraft", "protection"), 5)
                    .withLegacyEnchantment(0, 5)
                    .build();

            ItemStack legs = ItemStack.Builder
                    .of(version, new Identifier("minecraft", "golden_leggings"))
                    .withEnchantment(new Identifier("minecraft", "unbreaking"), 3)
                    .withLegacyEnchantment(34, 3)
                    .build();

            ItemStack feet = ItemStack.Builder
                    .of(version, new Identifier("minecraft", "diamond_boots"))
                    .withEnchantment(new Identifier("minecraft", "feather_falling"), 4)
                    .withLegacyEnchantment(2, 4)
                    .build();
            ItemStack sword = ItemStack.Builder
                    .of(version, new Identifier("minecraft", "stone_sword"))
                    .withEnchantment(new Identifier("minecraft", "sharpness"), 100)
                    .withLegacyEnchantment(16, 100)
                    .withName(Component.text("Test Sword").withColor(new Color(0xAF4EBE)))
                    .build();

            pl.setItem(Entity.EquipmentSlot.HEAD, hat);
            pl.setItem(Entity.EquipmentSlot.CHEST, shirt);
            pl.setItem(Entity.EquipmentSlot.LEGS, legs);
            pl.setItem(Entity.EquipmentSlot.FEET, feet);
            pl.setItem(Entity.EquipmentSlot.MAINHAND, sword);

            if(version.hasFeature(GameVersion.Feature.OFF_HAND)) {

                ItemStack cmd = ItemStack.Builder
                        .of(version, new Identifier("minecraft", "command_block"))
                        .withName(Component.text("Hello").withColor(new Color(0x398F3C)))
                        .build();

                pl.setItem(Entity.EquipmentSlot.OFFHAND, cmd);
            }

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }

    public static void messengerCmd(Player pl) {
        try {

            ServerMessengerModule mod = pl.getServer().getModuleManager().getModule(ServerMessengerModule.class);
            if(mod == null) {
                pl.sendMessage(Component.text("The messenger module is unloaded!").withColor(TextColor.RED));
                return;
            }

            Messenger messenger = mod.getMessenger();

            messenger.unsubscribe(pl, "_test");
            messenger.subscribe(pl, "_test", msg -> {
                pl.sendMessage(Component.text("[Test Message] " + msg.payloadAsString()));
                MidnightCoreAPI.LOGGER.info("[Test Message] " + msg.payloadAsString());
            });

            messenger.publish("_test", 10000, "Hello, World");

            pl.sendMessage(Component.text("Sent test message"));

        } catch (Throwable th) {
            MidnightCoreAPI.LOGGER.warn("An error occurred during a test command!", th);
        }
    }


}
