import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.midnightcore.api.text.*;

public class TestPlaceholders {

    public static String toTitleCase(String string) {

        StringBuilder builder = new StringBuilder();

        String[] words = string.replace('_', ' ').replace('-', ' ').split(" ");
        int i = 0;
        for(String word : words) {
            if(i++ > 0) {
                builder.append(" ");
            }
            builder.append(word.substring(0,1).toUpperCase()).append(word.substring(1));
        }

        return builder.toString();
    }

    static class UsernameHolder {
        MComponent username;

        public UsernameHolder(MComponent username) {
            this.username = username;
        }

        public MComponent getUsername() {
            return username;
        }
    }

    @Test
    public void testPlaceholders() {

        PlaceholderManager manager = PlaceholderManager.INSTANCE;
        manager.getInlinePlaceholders().register("to_title_case", PlaceholderSupplier.createWithParameter(TestPlaceholders::toTitleCase));
        manager.getPlaceholders().register("username", PlaceholderSupplier.create(UsernameHolder.class, UsernameHolder::getUsername));

        // Inline
        String toParse = "Hello, %to_title_case<%id%>%, World";
        String parsed = manager.applyInlinePlaceholders(toParse, CustomPlaceholderInline.create("id", "title_case"));

        Assertions.assertEquals("Hello, Title Case, World", parsed);

        // Percent signs
        toParse = "%name% 50%% Complete, %name%";
        parsed = manager.applyInlinePlaceholders(toParse, CustomPlaceholderInline.create("name", "test"));

        Assertions.assertEquals("test 50% Complete, test", parsed);

        // Component
        toParse = "&bHello, &f%to_title_case<%id%>%, %name%";
        MComponent parsedComponent = manager.parseText(toParse, CustomPlaceholderInline.create("id", "title_case"), CustomPlaceholder.create("name", new MTextComponent("Bingus")));

        Assertions.assertEquals("Hello, Title Case, Bingus", parsedComponent.getAllContent());
        Assertions.assertEquals("Hello, ", parsedComponent.getContent());


        // Percent signs (Component)
        toParse = "%name% 50%% Complete, %name%";
        parsedComponent = manager.parseText(toParse, CustomPlaceholder.create("name", new MTextComponent("test")));

        Assertions.assertEquals("test 50% Complete, test", parsedComponent.getAllContent());

        // With Context
        UsernameHolder holder = new UsernameHolder(new MTextComponent("User").withChild(new MTextComponent(" Name")));

        toParse = "&bHello, &f%to_title_case<%id%>%, %username%";
        parsedComponent = manager.parseText(toParse, holder, CustomPlaceholderInline.create("id", "title_case"));

        Assertions.assertEquals("Hello, Title Case, User Name", parsedComponent.getAllContent());
        Assertions.assertEquals("Hello, ", parsedComponent.getContent());
    }

}
