import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.midnightcore.api.text.*;

public class TestComponents {

    @Test
    public void testSerialization() {

        String json = "{\"text\":\"Hello\",\"color\":\"#ade012\",\"bold\":true,\"italic\":false,\"font\":\"minecraft:default\",\"extra\":[{\"text\":\" World\"}]}";

        MComponent comp = MComponent.parse(json);

        Assertions.assertEquals("Hello World", comp.getAllContent());
        Assertions.assertEquals("orange", TextColor.toDyeColor(comp.getStyle().getColor()));
        Assertions.assertEquals("minecraft:default", comp.getStyle().getFont().toString());
        Assertions.assertEquals(true, comp.getStyle().getBold());
        Assertions.assertEquals(false, comp.getStyle().getItalic());
        Assertions.assertNull(comp.getStyle().getObfuscated());

        String jsonTranslate = "{\"translate\":\"minecraft.item.iron_sword\",\"color\":\"#ade012\",\"font\":\"minecraft:default\"}";

        MComponent translate = MComponent.parseJSON(jsonTranslate);

        Assertions.assertEquals(jsonTranslate, translate.toJSONString());

        String legacyText = "&aHello World";
        MComponent legacyComp = MComponent.parse(legacyText);

        Assertions.assertEquals("green", TextColor.toLegacyColor(legacyComp.getStyle().getColor()));
        Assertions.assertEquals("Hello World", legacyComp.getAllContent());
        Assertions.assertEquals("{\"text\":\"Hello World\",\"color\":\"#55ff55\"}", legacyComp.toJSONString());
        Assertions.assertEquals("#55ff55Hello World", legacyComp.toConfigText());

        Assertions.assertEquals("Hello, W", new MTextComponent("Hello, World").subComponent(0, 8).getAllContent());

    }

    @Test
    public void testClickEvent() {

        String json = "{\"text\":\"Click Here: \",\"extra\":[{\"text\":\"[Click]\",\"bold\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/kill\"}}]}";
        MComponent comp = PlaceholderManager.INSTANCE.parseText(json);

        Assertions.assertEquals("Click Here: [Click]", comp.getAllContent());
        MComponent child1 = comp.getChildren().iterator().next();

        Assertions.assertNotNull(child1.getClickEvent());
        Assertions.assertEquals(MClickEvent.ClickAction.SUGGEST_COMMAND, child1.getClickEvent().getAction());
        Assertions.assertEquals("/kill", child1.getClickEvent().getValue());

        Assertions.assertEquals(comp.toConfigText(), comp.toJSONString());

    }

    @Test
    public void testHoverEvent() {

        String json = "{\"text\":\"Hover Here: \",\"extra\":[{\"text\":\"[Hover]\",\"bold\":true,\"color\":\"green\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"Hello\",\"color\":\"blue\"}}}]}";
        MComponent comp = MComponent.parseJSON(json);

        Assertions.assertEquals("Hover Here: [Hover]", comp.getAllContent());
        MComponent child1 = comp.getChildren().iterator().next();

        Assertions.assertNotNull(child1.getHoverEvent());
        Assertions.assertEquals(MHoverEvent.HoverAction.SHOW_TEXT, child1.getHoverEvent().getAction());
        Assertions.assertEquals("Hello", child1.getHoverEvent().getContentsAsText().getContent());

        Assertions.assertEquals(comp.toConfigText(), comp.toJSONString());

    }

    @Test
    public void testSubcomponent() {

        String json = "{\"text\":\"Hello\",\"color\":\"#ade012\",\"bold\":true,\"italic\":false,\"font\":\"minecraft:default\",\"extra\":[{\"text\":\" \"},{\"text\":\"World\"}]}";
        MComponent comp = MComponent.parse(json);

        // From Beginning
        MComponent hello1 = comp.subComponent(0, 2);
        Assertions.assertEquals("He", hello1.getAllContent());

        MComponent hello2 = comp.subComponent(0, 5);
        Assertions.assertEquals("Hello", hello2.getAllContent());

        MComponent hello3 = comp.subComponent(0, 6);
        Assertions.assertEquals("Hello ", hello3.getAllContent());

        MComponent hello4 = comp.subComponent(0, 7);
        Assertions.assertEquals("Hello W", hello4.getAllContent());


        // From end
        MComponent world1 = comp.subComponent(2, 11);
        Assertions.assertEquals("llo World", world1.getAllContent());

        MComponent world2 = comp.subComponent(5, 11);
        Assertions.assertEquals(" World", world2.getAllContent());

        MComponent world3 = comp.subComponent(6, 11);
        Assertions.assertEquals("World", world3.getAllContent());

        MComponent world4 = comp.subComponent(7, 11);
        Assertions.assertEquals("orld", world4.getAllContent());


        // Middle
        MComponent hewo1 = comp.subComponent(3,8);
        Assertions.assertEquals("lo Wo", hewo1.getAllContent());

        MComponent hewo2 = comp.subComponent(5,8);
        Assertions.assertEquals(" Wo", hewo2.getAllContent());

        MComponent hewo3 = comp.subComponent(3,6);
        Assertions.assertEquals("lo ", hewo3.getAllContent());

        MComponent hewo4 = comp.subComponent(2,4);
        Assertions.assertEquals("ll", hewo4.getAllContent());


        MComponent s1 = MComponent.parse("&f||||||||||||||||||||");
        MComponent s2 = MComponent.parse("&b||||||||||||||||||||");

        for(int i = 0 ; i < 11 ; i++) {

            float progress = (float) i / 10.0f;

            int emptyIndex = Math.max(0, (int) (progress * s1.getLength()));
            MComponent sub1 = s2.subComponent(emptyIndex, s1.getLength());

            int fullIndex = Math.max(0, (int) (progress * s2.getLength()));
            MComponent sub2 = s2.subComponent(0, fullIndex);

            Assertions.assertEquals(s1.getLength(), sub1.getLength() + sub2.getLength());
        }
    }
}
