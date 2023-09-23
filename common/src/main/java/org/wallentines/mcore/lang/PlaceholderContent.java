package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.Content;

import java.util.Collection;
import java.util.List;

public class PlaceholderContent extends Content {

    private final UnresolvedComponent component;
    private final Collection<Object> context;

    public PlaceholderContent(UnresolvedComponent component, Collection<Object> context) {
        super("placeholder");

        this.component = component;
        this.context = context;
    }

    public static Component component(String component) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), List.of()));
    }

    public static Component component(String component, Object... args) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), List.of(args)));
    }

    public static Component component(String component, Collection<Object> args) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), List.copyOf(args)));
    }

    public static Component component(UnresolvedComponent component) {
        return new Component(new PlaceholderContent(component, List.of()));
    }

    public static Component component(UnresolvedComponent component, Object... args) {
        return new Component(new PlaceholderContent(component, List.of(args)));
    }

    public static Component component(UnresolvedComponent component, Collection<Object> args) {
        return new Component(new PlaceholderContent(component, List.copyOf(args)));
    }

    public Component resolve(Object... args) {

        PlaceholderContext ctx;
        if(args == null || args.length == 0) {
            ctx = new PlaceholderContext();
        } else {
            ctx = new PlaceholderContext(List.of(args));
        }

        for(Object o : context) {
            ctx.addValue(o);
        }

        return component.resolve(ctx);
    }

    @Override
    public boolean requiresResolution() {
        return true;
    }
}
