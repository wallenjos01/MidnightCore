package org.wallentines.mcore.util;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Location;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.ClickEvent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.HoverEvent;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.List;
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
            pl.sendMessage(manager.component("test"));

            ItemStack is = ItemStack.Builder.woolWithColor(TextColor.RED)
                    .withCount(13)
                    .withName(org.wallentines.mcore.text.Component.text("Test").withColor(TextColor.GREEN))
                    .withLore(List.of(org.wallentines.mcore.text.Component.text("Bruh")))
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


            pl.teleport(new Location(new Identifier("minecraft", "the_nether"), 0, 100, 0, 0, 0));
            pl.giveItem(is);

        } catch (Throwable th) {
            th.printStackTrace();
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
            th.printStackTrace();
        }
    }

}
