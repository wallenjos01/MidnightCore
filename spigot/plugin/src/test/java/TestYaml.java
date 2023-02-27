import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.text.LangRegistry;
import org.wallentines.midnightcore.spigot.config.YamlCodec;

public class TestYaml {

    @Test
    public void testLoading() {

        DummyServer.register();

        String yaml =
            "key: value\n" +
            "number: 12\n" +
            "bool: true\n" +
            "list:\n" +
            "- value1\n" +
            "- 13\n" +
            "- false\n" +
            "section:\n" +
            "  hello: world";

        ConfigObject obj = YamlCodec.INSTANCE.decode(ConfigContext.INSTANCE, yaml);
        Assertions.assertTrue(obj.isSection());

        ConfigSection root = obj.asSection();
        Assertions.assertEquals(5, root.size());
        Assertions.assertEquals("value", root.getString("key"));
        Assertions.assertEquals(12, root.getNumber("number"));
        Assertions.assertTrue(root.getBoolean("bool"));

    }

    @Test
    public void testLangRegistry() {

        String yaml =
            "section1.group1.key1: Value1\n" +
            "section1.group1.key2: Value2\n" +
            "section1.group2.key1: Value3\n" +
            "section2.group1.key1: Value4";

        LangRegistry reg = LangRegistry.fromConfigSection(YamlCodec.INSTANCE.decode(ConfigContext.INSTANCE, yaml).asSection());
        Assertions.assertEquals("Value1", reg.getMessage("section1.group1.key1"));
        Assertions.assertEquals("Value2", reg.getMessage("section1.group1.key2"));
        Assertions.assertEquals("Value3", reg.getMessage("section1.group2.key1"));
        Assertions.assertEquals("Value4", reg.getMessage("section2.group1.key1"));

    }

}
