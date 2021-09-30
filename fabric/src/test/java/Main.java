import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Main {

    public static void main(String[] args) {

        ConfigRegistry.INSTANCE.registerProvider(JsonConfigProvider.INSTANCE);
        FileConfig conf = new FileConfig(new File("config.json"));

        Base64.Decoder dec = Base64.getDecoder();

        byte[] buffer = new byte[1024];
        int count;

        for(ConfigSection s : conf.getRoot().getListFiltered("skins", ConfigSection.class)) {


            String skin = s.getString("b64");
            String id = s.getString("id");

            File f = new File(id + ".png");
            if(f.exists()) continue;

            try {
                String json = new String(dec.decode(skin), StandardCharsets.UTF_8);
                ConfigSection data = JsonConfigProvider.INSTANCE.loadFromString(json);

                String urlStr = data.getSection("textures").getSection("SKIN").getString("url");

                URL url = new URL(urlStr);
                BufferedInputStream is = new BufferedInputStream(url.openStream());

                FileOutputStream os = new FileOutputStream(id + ".png");

                while((count = is.read(buffer,0,1024)) != -1) {
                    os.write(buffer, 0, count);
                }
                os.close();
                is.close();

            } catch (Exception ex) {

                System.out.println("An error occurred parsing skin " + id + "!");
                ex.printStackTrace();
            }

        }


    }

}
