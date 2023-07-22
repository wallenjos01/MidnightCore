package org.wallentines.mcore.text;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.LangContent;
import org.wallentines.midnightlib.registry.StringRegistry;

/**
 * An interface for resolving custom component types before sent to Players. Custom content types will need to be
 * resolved into a component type the server has the ability to interpret before they can be sent to players.
 * Components are resolved according to their content type. If a resolver is available for the component's content type,
 * it will be resolved before being sent to players.
 */
public interface ComponentResolver {

    /**
     * Resolves a component according to the given Player
     * @param other The component to resolve. Note that all children will be removed before resolution
     * @param player The player for whom this component is being resolved
     * @return A resolved component
     */
    Component resolve(Content other, Player player);

    /**
     * A registry for ComponentResolvers according to their Content type.
     */
    StringRegistry<ComponentResolver> REGISTRY = new StringRegistry<>();

    /**
     * Determines if a component can be resolved
     * @param comp The component to check
     * @return Whether the component can be resolved
     */
    static boolean canBeResolved(Component comp) {
        if(comp.content.requiresResolution()) return true;
        for(Component child : comp.children) {
            if(canBeResolved(child)) return true;
        }
        return false;
    }

    /**
     * Resolves a component and all of its children according to the given Player
     * @param comp The component to resolve
     * @param player The player
     * @return A resolved component
     */
    static Component resolveComponent(Component comp, Player player) {

        // Don't attempt to resolve if the component cannot be resolved
        if(!canBeResolved(comp)) return comp;

        Component out;
        ComponentResolver resolver = REGISTRY.get(comp.content.type);
        if(resolver == null) {
            out = Component.empty();
            MidnightCoreAPI.LOGGER.warn("Component with type " + comp.content.type + " requires resolution but no resolver was found!");
        } else {
            out = resolver.resolve(comp.content, player);
        }

        for(Component child : comp.children) {
            out.addChild(resolveComponent(child, player));
        }

        return out;
    }

    ComponentResolver LANG = REGISTRY.register("lang", (cnt, player) -> {

        LangContent lang = (LangContent) cnt;
        PlaceholderContext ctx = lang.getContext().copy();
        ctx.values.add(player);

        return lang.getLangManager().getMessage(lang.getKey(), player.getLanguage(), ctx);
    });

}
