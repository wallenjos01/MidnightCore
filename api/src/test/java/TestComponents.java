import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.midnightcore.api.text.*;

public class TestComponents {

    @Test
    public void testSerialization() {

        String json = "{\"text\":\"Hello\",\"color\":\"#ade012\",\"bold\":true,\"italic\":false,\"font\":\"minecraft:default\",\"extra\":[{\"text\":\" World\"}]}";

        MComponent comp = MComponent.parse(json);

        Assertions.assertEquals("Hello World", comp.getAllContent());
        Assertions.assertEquals("orange", comp.getStyle().getColor().toDyeColor());
        Assertions.assertEquals("minecraft:default", comp.getStyle().getFont().toString());
        Assertions.assertEquals(true, comp.getStyle().getBold());
        Assertions.assertEquals(false, comp.getStyle().getItalic());
        Assertions.assertNull(comp.getStyle().getObfuscated());

        String jsonTranslate = "{\"translate\":\"minecraft.item.iron_sword\",\"color\":\"#ade012\",\"font\":\"minecraft:default\"}";

        MComponent translate = MComponent.parseJSON(jsonTranslate);

        Assertions.assertEquals(jsonTranslate, translate.toJSONString());

        String legacyText = "&aHello World";
        MComponent legacyComp = MComponent.parse(legacyText);

        Assertions.assertEquals("green", legacyComp.getStyle().getColor().toLegacyColor());
        Assertions.assertEquals("Hello World", legacyComp.getAllContent());
        Assertions.assertEquals("{\"text\":\"Hello World\",\"color\":\"#55ff55\"}", legacyComp.toJSONString());
        Assertions.assertEquals("#55ff55Hello World", legacyComp.toConfigText());

        Assertions.assertEquals("Hello, W", new MTextComponent("Hello, World").subComponent(0, 8).getAllContent());

    }
}
