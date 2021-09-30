package me.m1dnightninja.midnightcore.velocity.module.lang;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangModule;
import me.m1dnightninja.midnightcore.velocity.player.VelocityPlayer;

public class LangModule extends AbstractLangModule {

    @Override
    public String getPlayerLocale(MPlayer u) {

        VelocityPlayer pl = (VelocityPlayer) u;
        return pl.getVelocityPlayer().getPlayerSettings().getLocale().toString();
    }
}
