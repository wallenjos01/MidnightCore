package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.*;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.*;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public class TestLang {

    private static class DummyPlayer implements Player {

        @Override
        public UUID getUUID() {
            return null;
        }
        @Override
        public Identifier getType() { return null; }
        @Override
        public Component getDisplayName() {
            return null;
        }
        @Override
        public Identifier getDimensionId() {
            return null;
        }
        @Override
        public Vec3d getPosition() {
            return null;
        }
        @Override
        public float getYaw() {
            return 0;
        }
        @Override
        public float getPitch() {
            return 0;
        }
        @Override
        public boolean isRemoved() { return false; }
        @Override
        public void teleport(Location location) { }
        @Override
        public String getUsername() {
            return "dummy";
        }
        @Override
        public Server getServer() { return null; }
        @Override
        public void sendMessage(Component component) { }
        @Override
        public void sendActionBar(Component component) { }
        @Override
        public void sendTitle(Component title) { }
        @Override
        public void sendSubtitle(Component title) { }
        @Override
        public void clearTitles() { }
        @Override
        public void setTitleTimes(int fadeIn, int stay, int fadeOut) { }
        @Override
        public void resetTitles() { }
        @Override
        public ItemStack getHandItem() {
            return null;
        }
        @Override
        public ItemStack getOffhandItem() {
            return null;
        }
        @Override
        public void giveItem(ItemStack item) { }
        @Override
        public String getLanguage() {
            return "en_us";
        }
        @Override
        public GameMode getGameMode() { return null; }
        @Override
        public void setGameMode(GameMode mode) { }
        @Override
        public boolean isOnline() { return false; }
        @Override
        public Skin getSkin() { return null; }
    }

    @Test
    public void testLang() {

        PlaceholderManager plMan = new PlaceholderManager();
        plMan.registerSupplier("player_name", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Player.class, Player::getUsername, "")));

        LangRegistry registry = new LangRegistry();
        registry.register("test", UnresolvedComponent.parse("Hello, world %player_name%", plMan).getOrThrow());
        LangManager manager = new LangManager(registry, null);

        Player pl = new DummyPlayer();

        Component got = manager.getMessage("test", pl.getLanguage(), new PlaceholderContext(pl));
        Component cmp = manager.component("test");

        cmp = ComponentResolver.resolveComponent(cmp, pl);

        Assertions.assertEquals(got, cmp);

    }

}
