package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.math.Color;

public class TestUnresolved {

    @Test
    public void testUnresolvedComponent() {

        PlaceholderManager plm = new PlaceholderManager();
        plm.registerSupplier("placeholder", PlaceholderSupplier.inline(ctx -> "World"));

        SerializeResult<UnresolvedComponent> result = UnresolvedComponent.parse("Hello, %placeholder%");
        Assertions.assertTrue(result.isComplete());

        UnresolvedComponent comp = result.getOrThrow();

        Assertions.assertEquals("Hello, %placeholder%", comp.toRaw());
        Assertions.assertEquals("Hello, World", comp.resolveFlat(plm, new PlaceholderContext()));
        Assertions.assertEquals(Component.text("Hello, World"), comp.resolve(plm, new PlaceholderContext()));


        UnresolvedComponent comp2 = plm.parse("Hello, %placeholder%");

        Assertions.assertEquals("Hello, %placeholder%", comp2.toRaw());
        Assertions.assertEquals("Hello, World", comp2.resolveFlat(plm, new PlaceholderContext()));
        Assertions.assertEquals(Component.text("Hello, World"), comp2.resolve(plm, new PlaceholderContext()));


        Assertions.assertEquals(comp, comp2);

    }

    @Test
    public void testComponent() {

        PlaceholderManager plm = new PlaceholderManager();

        Color color = Color.fromRGBI(10);
        plm.registerSupplier("color", PlaceholderSupplier.inline(ctx -> color.toHex()));
        plm.registerSupplier("name", PlaceholderSupplier.of(ctx -> Component.text("Name").withColor(Color.fromRGBI(11))));

        UnresolvedComponent un = plm.parse("&%color%&lHello&f, %name%!");
        Component cmp = un.resolve(plm, new PlaceholderContext());

        Assertions.assertEquals("Hello, Name!", cmp.allText());
        Assertions.assertEquals(
                Component.empty().withColor(color)
                        .addChild(Component.text("Hello").withBold(true))
                        .addChild(Component.text(", ").withColor(Color.WHITE)
                            .addChild(Component.text("Name").withColor(Color.fromRGBI(11)))
                            .addChild(Component.text("!"))
                        ),
                cmp
        );

    }

    @Test
    public void testFormatting() {

        PlaceholderManager plm = new PlaceholderManager();
        plm.registerSupplier("name", PlaceholderSupplier.of(ctx -> Component.text("Name").withItalic(true)));

        UnresolvedComponent un = plm.parse("&aHello, %name%!");
        Component cmp = un.resolve(plm, new PlaceholderContext());

        Assertions.assertEquals("Hello, Name!", cmp.allText());
        Assertions.assertEquals(
                Component.text("Hello, ").withColor(Color.fromRGBI(10))
                        .addChild(Component.text("Name").withItalic(true))
                        .addChild(Component.text("!")),
                cmp
        );

    }


    @Test
    public void testUnicode() {

        String unparsed = "%placeholder%: \u00bb";

        SerializeResult<UnresolvedComponent> parsed = UnresolvedComponent.parse(unparsed);
        Assertions.assertTrue(parsed.isComplete());

        Assertions.assertEquals("%placeholder%: \u00bb", parsed.getOrThrow().toRaw());

    }


}
