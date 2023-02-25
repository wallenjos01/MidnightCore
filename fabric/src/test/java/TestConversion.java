import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.contents.LiteralContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MHoverEvent;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;

public class TestConversion {

    @Test
    public void testComponent() {

        String json = "{\"text\":\"Hover Here\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"Hello\",\"color\":\"blue\"}}}";
        MComponent comp = MComponent.parseJSON(json);

        Assertions.assertEquals("Hover Here", comp.getAllContent());

        Assertions.assertNotNull(comp.getHoverEvent());
        Assertions.assertEquals(MHoverEvent.HoverAction.SHOW_TEXT, comp.getHoverEvent().getAction());
        Assertions.assertEquals("Hello", comp.getHoverEvent().getContentsAsText().getContent());

        Assertions.assertEquals(comp.toConfigText(), comp.toJSONString());

        Component mccomp = ConversionUtil.toComponent(comp);
        Assertions.assertNotNull(mccomp.getStyle().getHoverEvent());
        Assertions.assertEquals(HoverEvent.Action.SHOW_TEXT, mccomp.getStyle().getHoverEvent().getAction());
        Assertions.assertEquals("Hello", ((LiteralContents) mccomp.getStyle().getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT).getContents()).text());

    }

}
