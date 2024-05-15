package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.lang.*;
import org.wallentines.mcore.text.Component;

public class TestLang {

    private static class Dummy implements LocaleHolder {

        public String getUsername() {
            return "Username";
        }
        @Override
        public String getLanguage() {
            return "en_us";
        }
    }
    @Test
    public void testLang() {

        PlaceholderManager plMan = new PlaceholderManager();
        plMan.registerSupplier("player_name", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Dummy.class, Dummy::getUsername, "")));

        LangRegistry registry = new LangRegistry(plMan);
        registry.register("test", UnresolvedComponent.parse("Hello, world %player_name%").getOrThrow());
        LangManager manager = new LangManager(registry, null);
        LangManager.registerPlaceholders(plMan);

        Dummy pl = new Dummy();

        Component got = manager.getMessage("test", pl.getLanguage(), pl);
        Component cmp = manager.component("test", pl).resolve(plMan);

        Assertions.assertEquals(got, cmp);

    }

}
