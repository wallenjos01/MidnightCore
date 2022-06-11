import org.junit.Assert;
import org.junit.Test;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.io.File;
import java.util.Random;

public class TestComponents {

    @Test
    public void testSerialization() {

        new MidnightCoreAPI() {

            @Override
            public ConfigSection getConfig() {
                return null;
            }

            @Override
            public File getDataFolder() {
                return null;
            }

            @Override
            public Version getGameVersion() {
                return Version.SERIALIZER.deserialize("1.18.1");
            }

            @Override
            public ModuleManager<MidnightCoreAPI> getModuleManager() {
                return null;
            }

            @Override
            public Registry<RequirementType<MPlayer>> getRequirementRegistry() {
                return null;
            }

            @Override
            public PlayerManager getPlayerManager() {
                return null;
            }

            @Override
            public MItemStack createItem(Identifier id, int count, ConfigSection nbt) {
                return null;
            }

            @Override
            public InventoryGUI createGUI(MComponent title) {
                return null;
            }

            @Override
            public CustomScoreboard createScoreboard(String id, MComponent title) {
                return null;
            }

            @Override
            public void executeConsoleCommand(String command, boolean log) {

            }

            @Override
            public Random getRandom() {
                return null;
            }
        };


        ConfigRegistry.INSTANCE.setupDefaults("minecraft");
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

    }
}
