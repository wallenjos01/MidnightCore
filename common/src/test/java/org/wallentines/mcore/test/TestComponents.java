package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.Content;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.midnightlib.registry.Identifier;

public class TestComponents {

    @Test
    public void testText() {

        Component component = Component.text("Hello");

        Assertions.assertNotNull(component);
        Assertions.assertNotNull(component.content);

        Assertions.assertNull(component.color);
        Assertions.assertNull(component.bold);
        Assertions.assertNull(component.italic);
        Assertions.assertNull(component.underlined);
        Assertions.assertNull(component.strikethrough);
        Assertions.assertNull(component.obfuscated);
        Assertions.assertNull(component.reset);
        Assertions.assertNull(component.insertion);
        Assertions.assertNull(component.hoverEvent);
        Assertions.assertNull(component.clickEvent);

        Assertions.assertTrue(component.children.isEmpty());
        Assertions.assertTrue(component.content instanceof Content.Text);

    }

    @Test
    public void testTranslate() {

        Component component = Component.translate("an.example.key");

        Assertions.assertNotNull(component);
        Assertions.assertNotNull(component.content);

        Assertions.assertNull(component.color);
        Assertions.assertNull(component.bold);
        Assertions.assertNull(component.italic);
        Assertions.assertNull(component.underlined);
        Assertions.assertNull(component.strikethrough);
        Assertions.assertNull(component.obfuscated);
        Assertions.assertNull(component.reset);
        Assertions.assertNull(component.insertion);
        Assertions.assertNull(component.hoverEvent);
        Assertions.assertNull(component.clickEvent);

        Assertions.assertTrue(component.children.isEmpty());
        Assertions.assertTrue(component.content instanceof Content.Translate);

    }

    @Test
    public void testKeyBind() {

        Component component = Component.keybind("an.example.bind");

        Assertions.assertNotNull(component);
        Assertions.assertNotNull(component.content);

        Assertions.assertNull(component.color);
        Assertions.assertNull(component.bold);
        Assertions.assertNull(component.italic);
        Assertions.assertNull(component.underlined);
        Assertions.assertNull(component.strikethrough);
        Assertions.assertNull(component.obfuscated);
        Assertions.assertNull(component.reset);
        Assertions.assertNull(component.insertion);
        Assertions.assertNull(component.hoverEvent);
        Assertions.assertNull(component.clickEvent);

        Assertions.assertTrue(component.children.isEmpty());
        Assertions.assertTrue(component.content instanceof Content.Keybind);
    }

    @Test
    public void testImmutability() {

        Component component = Component.text("Hello");
        Assertions.assertNull(component.color);

        Component changed = component.withColor(TextColor.AQUA);
        Assertions.assertNull(component.color);
        Assertions.assertNotNull(changed.color);

    }

    @Test
    public void testFunctions() {

        Component cmp = Component.text("Hello").withBold(true);

        Assertions.assertEquals("Hello", cmp.text());
        Assertions.assertEquals("Hello", cmp.allText());
        Assertions.assertTrue(cmp.hasFormatting());
        Assertions.assertFalse(cmp.hasNonLegacyComponents());

        cmp = cmp.withFont(new Identifier("test", "cool_font"));

        Assertions.assertEquals("Hello", cmp.text());
        Assertions.assertEquals("Hello", cmp.allText());
        Assertions.assertTrue(cmp.hasFormatting());
        Assertions.assertTrue(cmp.hasNonLegacyComponents());

        cmp = cmp.addChild(Component.text(", World"));

        Assertions.assertEquals("Hello", cmp.text());
        Assertions.assertEquals("Hello, World", cmp.allText());
        Assertions.assertTrue(cmp.hasFormatting());
        Assertions.assertTrue(cmp.hasNonLegacyComponents());

    }

    @Test
    public void testCopying() {
        Component comp = Component.text("Hello, World").withColor(TextColor.RED);
        Assertions.assertEquals(comp, comp.copy());
    }

}
