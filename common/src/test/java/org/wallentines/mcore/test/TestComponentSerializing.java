package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.text.*;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Color;

import java.util.Arrays;

public class TestComponentSerializing {

    @Test
    public void testParsing() {

        String unparsedLegacy = "&6Hello, &dWorld";
        Component comp = Component.parse(unparsedLegacy);

        Assertions.assertEquals(TextColor.GOLD, comp.color);
        Assertions.assertEquals(1, comp.children.size());
        Assertions.assertTrue(comp.content instanceof Content.Text);

        Assertions.assertEquals(TextColor.LIGHT_PURPLE, comp.children.get(0).color);
        Assertions.assertEquals(0, comp.children.get(0).children.size());
        Assertions.assertTrue(comp.children.get(0).content instanceof Content.Text);

    }

    @Test
    public void testLegacySerializer() {

        ConfigPrimitive unparsed = new ConfigPrimitive("\u00A7aTest \u00A7bParsing");
        SerializeResult<Component> parsed = LegacySerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, unparsed);

        Assertions.assertTrue(parsed.isComplete());

        Component comp = parsed.getOrThrow();
        Assertions.assertEquals(TextColor.GREEN, comp.color);
        Assertions.assertEquals(1, comp.children.size());
        Assertions.assertTrue(comp.content instanceof Content.Text);

        Assertions.assertEquals(TextColor.AQUA, comp.children.get(0).color);
        Assertions.assertEquals(0, comp.children.get(0).children.size());
        Assertions.assertTrue(comp.children.get(0).content instanceof Content.Text);

        String serialized = comp.toLegacyText();
        String serialized2 = LegacySerializer.INSTANCE.serialize(ConfigContext.INSTANCE, comp).getOrThrow().asString();

        Assertions.assertEquals(serialized, unparsed.asString());
        Assertions.assertEquals(serialized2, unparsed.asString());

    }


    @Test
    public void testModernSerializer() {

        Serializer<Component> ser = ModernSerializer.INSTANCE.forContext(GameVersion.MAX);
        testModern(ser);


        // Complex
        Component cmp = Component.text("Hello")
                .withColor(TextColor.GREEN)
                .withBold(true)
                .addChild(Component.translate("item.minecraft.diamond_sword")
                        .withHoverEvent(HoverEvent.create(Component.text("Test"))));

        ConfigSection serialized = ser.serialize(ConfigContext.INSTANCE, cmp).getOrThrow().asSection();

        Component comp = ser.deserialize(ConfigContext.INSTANCE, serialized).getOrThrow();

        Assertions.assertEquals(cmp, comp);

    }

    private void testModern(Serializer<Component> serializer) {

        // String
        ConfigPrimitive unparsedString = new ConfigPrimitive("Hello");
        SerializeResult<Component> parsedString = serializer.deserialize(ConfigContext.INSTANCE, unparsedString);

        Assertions.assertTrue(parsedString.isComplete());

        Component comp = parsedString.getOrThrow();
        Assertions.assertNull(comp.color);
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(0, comp.children.size());


        // Number
        ConfigPrimitive unparsedNumber = new ConfigPrimitive(33.5);
        SerializeResult<Component> parsedNumber = serializer.deserialize(ConfigContext.INSTANCE, unparsedNumber);

        Assertions.assertTrue(parsedNumber.isComplete());

        comp = parsedNumber.getOrThrow();
        Assertions.assertNull(comp.color);
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(0, comp.children.size());


        // Bool
        ConfigPrimitive unparsedBool = new ConfigPrimitive(true);
        SerializeResult<Component> parsedBool = serializer.deserialize(ConfigContext.INSTANCE, unparsedBool);

        Assertions.assertTrue(parsedBool.isComplete());

        comp = parsedBool.getOrThrow();
        Assertions.assertNull(comp.color);
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(0, comp.children.size());


        // Object
        ConfigSection unparsedObject = new ConfigSection().with("text", "Hello").with("color", "green");
        SerializeResult<Component> parsedObject = serializer.deserialize(ConfigContext.INSTANCE, unparsedObject);

        Assertions.assertTrue(parsedObject.isComplete());

        comp = parsedObject.getOrThrow();
        Assertions.assertEquals(TextColor.GREEN, comp.color);
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(0, comp.children.size());


        // List
        ConfigList unparsedList = new ConfigList().append(unparsedObject).append(unparsedObject);
        SerializeResult<Component> parsedList = serializer.deserialize(ConfigContext.INSTANCE, unparsedList);

        Assertions.assertTrue(parsedList.isComplete());

        comp = parsedList.getOrThrow();
        Assertions.assertEquals(TextColor.GREEN, comp.color);
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(1, comp.children.size());

        Assertions.assertEquals(TextColor.GREEN, comp.children.get(0).color);
        Assertions.assertEquals(0, comp.children.get(0).children.size());
        Assertions.assertTrue(comp.children.get(0).content instanceof Content.Text);

    }

    @Test
    public void testConfigSerializer() {

        ConfigPrimitive unparsed = new ConfigPrimitive("&aHello, &#354a56World");
        SerializeResult<Component> parsed = ConfigSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, unparsed);

        Assertions.assertTrue(parsed.isComplete());

        Component comp = parsed.getOrThrow();
        Assertions.assertEquals(TextColor.GREEN, comp.color);
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(1, comp.children.size());

        Assertions.assertEquals(new Color(0x354a56), comp.children.get(0).color);
        Assertions.assertEquals(0, comp.children.get(0).children.size());
        Assertions.assertTrue(comp.children.get(0).content instanceof Content.Text);


        testModern(ConfigSerializer.INSTANCE);


        unparsed = new ConfigPrimitive("&aHello, #354a56World");
        parsed = ConfigSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, unparsed);

        Assertions.assertTrue(parsed.isComplete());

        comp = parsed.getOrThrow();
        Assertions.assertEquals(TextColor.GREEN, comp.color);
        Assertions.assertEquals("Hello, #354a56World", comp.text());
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(0, comp.children.size());


        unparsed = new ConfigPrimitive("&aHello, &&6World");
        parsed = ConfigSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, unparsed);

        Assertions.assertTrue(parsed.isComplete());

        comp = parsed.getOrThrow();
        Assertions.assertEquals(TextColor.GREEN, comp.color);
        Assertions.assertEquals("Hello, &6World", comp.text());
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals(0, comp.children.size());
    }

    @Test
    public void testPlain() {


        ConfigPrimitive unparsed = new ConfigPrimitive("{\"text\":\"Hello, World\",\"color\":\"gold\"}");
        SerializeResult<Component> parsed = PlainSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, unparsed);

        Assertions.assertTrue(parsed.isComplete());

        Component comp = parsed.getOrThrow();
        Assertions.assertTrue(comp.content instanceof Content.Text);
        Assertions.assertEquals("{\"text\":\"Hello, World\",\"color\":\"gold\"}", ((Content.Text) comp.content).text);

    }

    @Test
    public void testToString() {

        // Set the current protocol version to match Minecraft 1.20.1's
        Common.VERSION.setProtocolVersion(763);

        Component created = Component.text("Hello").withColor(TextColor.RED).withChildren(Arrays.asList(Component.text(", World")));

        String plain = created.allText();
        Assertions.assertEquals("Hello, World", plain);

        String json = created.toJSONString();
        Assertions.assertEquals("{\"text\":\"Hello\",\"color\":\"#ff5555\",\"extra\":[{\"text\":\", World\"}]}", json);

        String cfg = created.toConfigText();
        Assertions.assertEquals("&#ff5555Hello, World", cfg);

        String legacy = created.toLegacyText();
        Assertions.assertEquals("\u00A7cHello, World", legacy);

    }

}
