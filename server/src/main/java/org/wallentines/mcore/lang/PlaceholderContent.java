package org.wallentines.mcore.lang;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.Content;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class PlaceholderContent extends Content {

    private final UnresolvedComponent component;
    private final Function<Player, Collection<Object>> args;

    public PlaceholderContent(UnresolvedComponent component, Function<Player, Collection<Object>> args) {
        super("placeholder");

        this.component = component;
        this.args = args;
    }

    public static Component component(String component) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), null));
    }

    public static Component component(String component, Object... args) {
        List<Object> lst = List.of(args);
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), pl -> lst));
    }

    public static Component component(String component, Function<Player, Collection<Object>> args) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), args));
    }


    public static Component component(UnresolvedComponent component) {
        return new Component(new PlaceholderContent(component, null));
    }

    public static Component component(UnresolvedComponent component, Object... args) {
        List<Object> lst = List.of(args);
        return new Component(new PlaceholderContent(component, pl -> lst));
    }

    public static Component component(UnresolvedComponent component, Function<Player, Collection<Object>> args) {
        return new Component(new PlaceholderContent(component, args));
    }

    public Component resolve(Player player) {

        PlaceholderContext ctx;
        if(args == null) {
            ctx = new PlaceholderContext();
        } else {
            ctx = new PlaceholderContext(args.apply(player));
        }
        ctx.addValue(player);

        return component.resolve(ctx);
    }

    @Override
    public boolean requiresResolution() {
        return true;
    }
}
