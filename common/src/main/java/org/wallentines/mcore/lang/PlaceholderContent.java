package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.Content;

import java.util.Collection;
import java.util.List;

/**
 * A specialized component content type which contains an unresolved component
 */
public class PlaceholderContent extends Content {

    private final UnresolvedComponent component;
    private final Collection<Object> context;

    /**
     * Creates a new PlaceholderContent with the given unresolved component and context
     * @param component The component which will be resolved
     * @param context Additional context by which to resolve the component
     */
    public PlaceholderContent(UnresolvedComponent component, Collection<Object> context) {
        super("placeholder");

        this.component = component;
        this.context = context;
    }

    /**
     * Creates a component with a placeholder content with no arguments by parsing the given string
     * @param component The string to parse into an unresolved component
     * @return A new component
     */
    public static Component component(String component) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), List.of()));
    }

    /**
     * Creates a component with a placeholder content with arguments by parsing the given string
     * @param component The string to parse into an unresolved component
     * @param args Additional arguments to use during resolution
     * @return A new component
     */
    public static Component component(String component, Object... args) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), List.of(args)));
    }

    /**
     * Creates a component with a placeholder content with arguments by parsing the given string
     * @param component The string to parse into an unresolved component
     * @param args Additional arguments to use during resolution
     * @return A new component
     */
    public static Component componentWith(String component, Collection<Object> args) {
        return new Component(new PlaceholderContent(UnresolvedComponent.parse(component).getOrThrow(), List.copyOf(args)));
    }

    /**
     * Creates a component with a placeholder content with no arguments using the given component
     * @param component The component to resolve
     * @return A new component
     */
    public static Component component(UnresolvedComponent component) {
        return new Component(new PlaceholderContent(component, List.of()));
    }

    /**
     * Creates a component with a placeholder content with arguments using the given component
     * @param component The component to resolve
     * @param args Additional arguments to use during resolution
     * @return A new component
     */
    public static Component component(UnresolvedComponent component, Object... args) {
        return new Component(new PlaceholderContent(component, List.of(args)));
    }


    /**
     * Creates a component with a placeholder content with arguments using the given component
     * @param component The component to resolve
     * @param args Additional arguments to use during resolution
     * @return A new component
     */
    public static Component componentWith(UnresolvedComponent component, Collection<Object> args) {
        return new Component(new PlaceholderContent(component, List.copyOf(args)));
    }

    /**
     * Resolves the content according to the given arguments
     * @param args The arguments to resolve the contents by
     * @return A resolved component
     */
    public Component resolve(Object... args) {

        PlaceholderContext ctx = new PlaceholderContext(context);

        if(args != null) {
            for(Object o : args) {
                ctx.addValue(o);
            }
        }

        return component.resolve(ctx);
    }

    @Override
    public boolean requiresResolution() {
        return true;
    }
}
