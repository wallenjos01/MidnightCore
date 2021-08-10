import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        ConfigSection section = new ConfigSection();

        section.set("owo", 1);
        section.set("uwu", 2);
        section.set("ewe", 3);
        section.set("iwi", 4);
        section.set("awa", 5);
        section.set("owa", 6);

        for(String s : section.getKeys()) {
            System.out.println(s);
        }

        JsonConfigProvider.INSTANCE.saveToFile(section, new File("test.json"));

    }

}
