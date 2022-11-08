import org.junit.Assert;
import org.junit.Test;

import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

public class TestComponents {

    @Test
    public void testSerialization() {

        ConfigRegistry.INSTANCE.setupDefaults("minecraft", JsonConfigProvider.INSTANCE);
        ConfigRegistry.INSTANCE.registerInlineSerializer(TextColor.class, TextColor.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MStyle.class, MStyle.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MClickEvent.class, MClickEvent.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MHoverEvent.class, MHoverEvent.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MComponent.class, MComponent.SERIALIZER);

        String json = "{\"text\":\"Hello\",\"color\":\"#ade012\",\"bold\":true,\"italic\":false,\"font\":\"minecraft:default\",\"extra\":[{\"text\":\" World\"}]}";

        MComponent comp = MComponent.parse(json);

        Assert.assertEquals("Hello World", comp.getAllContent());
        Assert.assertEquals("orange", comp.getStyle().getColor().toDyeColor());
        Assert.assertEquals("minecraft:default", comp.getStyle().getFont().toString());
        Assert.assertEquals(true, comp.getStyle().getBold());
        Assert.assertEquals(false, comp.getStyle().getItalic());
        Assert.assertNull(comp.getStyle().getObfuscated());

        String jsonTranslate = "{\"translate\":\"minecraft.item.iron_sword\",\"color\":\"#ade012\",\"font\":\"minecraft:default\"}";

        MComponent translate = MComponent.SERIALIZER.deserialize(JsonConfigProvider.INSTANCE.loadFromString(jsonTranslate));

        Assert.assertEquals(jsonTranslate, MComponent.SERIALIZER.serialize(translate).toString());

        String legacyText = "&aHello World";
        MComponent legacyComp = MComponent.parse(legacyText);

        Assert.assertEquals("green", legacyComp.getStyle().getColor().toLegacyColor());
        Assert.assertEquals("Hello World", legacyComp.getAllContent());
        Assert.assertEquals("{\"text\":\"Hello World\",\"color\":\"#55ff55\"}", MComponent.SERIALIZER.serialize(legacyComp).toString());
        Assert.assertEquals("#55ff55Hello World", legacyComp.toConfigText());

        Assert.assertEquals("Hello, W", new MTextComponent("Hello, World").subComponent(0, 8).getAllContent());

    }
}
