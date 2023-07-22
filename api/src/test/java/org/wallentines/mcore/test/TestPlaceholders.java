package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.TextColor;

public class TestPlaceholders {

    @Test
    public void testParsing() {

        String unparsed = "&6Hello, %username%. Goodbye, %display_name%";

        Component dName = Component.text("Display Name").withColor(TextColor.GREEN);

        PlaceholderManager manager = new PlaceholderManager();
        manager.registerSupplier("username", PlaceholderSupplier.inline(ctx -> "Name"));
        manager.registerSupplier("display_name", PlaceholderSupplier.of(ctx -> dName));

        Component parsed = manager.parseAndResolve(unparsed, new PlaceholderContext());

        Assertions.assertEquals(Component.text("Hello, Name. Goodbye, ").withColor(TextColor.GOLD).addChild(dName), parsed);

    }

    @Test
    public void testParsingJSON() {

        String unparsed = "{\"text\":\"Hello, %username%. Goodbye, %display_name%\",\"color\":\"gold\"}";

        Component dName = Component.text("Display Name").withColor(TextColor.GREEN);

        PlaceholderManager manager = new PlaceholderManager();
        manager.registerSupplier("username", PlaceholderSupplier.inline(ctx -> "Name"));
        manager.registerSupplier("display_name", PlaceholderSupplier.of(ctx -> dName));

        Component parsed = manager.parseAndResolve(unparsed, new PlaceholderContext(), true);

        Assertions.assertEquals(Component.text("Hello, Name. Goodbye, ").withColor(TextColor.GOLD).addChild(dName), parsed);

    }

    @Test
    public void testParsingArguments() {

        String unparsed = "&6Hello, %arg<Test %username%>%";

        PlaceholderManager manager = new PlaceholderManager();
        manager.registerSupplier("username", PlaceholderSupplier.inline(ctx -> "Name"));
        manager.registerSupplier("arg", PlaceholderSupplier.withParameter(ctx -> ctx.parameter.withColor(TextColor.RED)));

        Component parsed = manager.parseAndResolve(unparsed, new PlaceholderContext());

        Assertions.assertEquals(Component.text("Hello, ").addChild(Component.text("Test Name").withColor(TextColor.RED)).withColor(TextColor.GOLD), parsed);

    }

}
