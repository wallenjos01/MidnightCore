package org.wallentines.mcore.lang;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.Content;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class LangContent extends Content {


    private final LangManager manager;
    private final String key;
    private final Function<Player, Collection<Object>> args;

    public LangContent(LangManager manager, String key, Function<Player, Collection<Object>> args) {
        super("lang");
        this.manager = manager;
        this.key = key;
        this.args = args;
    }

    public LangManager getLangManager() {
        return manager;
    }

    public String getKey() {
        return key;
    }

    public static Component component(LangManager manager, String key) {
        return new Component(new LangContent(manager, key, null));
    }

    public static Component component(LangManager manager, String key, Object... args) {
        List<Object> lst = List.of(args);
        return new Component(new LangContent(manager, key, pl -> lst));
    }

    public static Component component(LangManager manager, String key, Function<Player, Collection<Object>> args) {
        return new Component(new LangContent(manager, key, args));
    }

    public Component resolve(Player player) {

        PlaceholderContext ctx;
        if(args == null) {
            ctx = new PlaceholderContext();
        } else {
            ctx = new PlaceholderContext(args.apply(player));
        }
        ctx.values.add(player);

        return manager.getMessage(key, player.getLanguage(), ctx);
    }

    @Override
    public boolean requiresResolution() {
        return true;
    }
}
