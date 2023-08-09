package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.SerializeResult;

public class TestUnresolved {

    @Test
    public void testUnresolvedComponent() {

        PlaceholderManager plm = new PlaceholderManager();
        plm.registerSupplier("placeholder", PlaceholderSupplier.inline(ctx -> "World"));

        SerializeResult<UnresolvedComponent> result = UnresolvedComponent.parse("Hello, %placeholder%", plm);
        Assertions.assertTrue(result.isComplete());

        UnresolvedComponent comp = result.getOrThrow();

        Assertions.assertEquals("Hello, %placeholder%", comp.toRaw());
        Assertions.assertEquals("Hello, World", comp.resolveFlat(new PlaceholderContext()));
        Assertions.assertEquals(Component.text("Hello, World"), comp.resolve(new PlaceholderContext()));


        UnresolvedComponent comp2 = plm.parse("Hello, %placeholder%");

        Assertions.assertEquals("Hello, %placeholder%", comp2.toRaw());
        Assertions.assertEquals("Hello, World", comp2.resolveFlat(new PlaceholderContext()));
        Assertions.assertEquals(Component.text("Hello, World"), comp2.resolve(new PlaceholderContext()));


        Assertions.assertEquals(comp, comp2);

    }


}
