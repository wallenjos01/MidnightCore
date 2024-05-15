package org.wallentines.mcore.text;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.lang.PlaceholderContent;
import org.wallentines.midnightlib.registry.StringRegistry;


/**
 * An interface for resolving custom component types before sent to Players. Custom content types will need to be
 * resolved into a component type the server has the ability to interpret before they can be sent to players.
 * Components are resolved according to their content type. If a resolver is available for the component's content type,
 * it will be resolved before being sent to players.
 */
@Deprecated
public interface ComponentResolver {

    /**
     * Resolves a component according to the given Player
     * @param other The component to resolve. Note that all children will be removed before resolution
     * @param context The context by which to resolve the content
     * @return A resolved component
     */
    Component resolve(Content other, Object... context);

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
     * Resolves a component and all of its children according to the given context
     * @param comp The component to resolve
     * @param context The context
     * @return A resolved component
     */
    static Component resolveComponent(Component comp, Object... context) {

        // Don't attempt to resolve if the component cannot be resolved
        if(!canBeResolved(comp)) return comp;

        Component out;
        if(comp.content.requiresResolution()) {
            ComponentResolver resolver = REGISTRY.get(comp.content.type);
            if (resolver == null) {
                out = Component.empty();
                MidnightCoreAPI.LOGGER.warn("Component with type " + comp.content.type + " requires resolution but no resolver was found!");
            } else {
                out = resolver.resolve(comp.content, context);
            }
        } else {
            out = comp.baseCopy();
        }

        for(Component child : comp.children) {
            out = out.addChild(resolveComponent(child, context));
        }

        return out;
    }

    ComponentResolver LANG = REGISTRY.register("lang", (cnt, ctx) -> {
        LangContent lng = (LangContent) cnt;
        return lng.resolve(ctx);
    });
    ComponentResolver PLACEHOLDER = REGISTRY.register("placeholder", (cnt, ctx) -> {
        PlaceholderContent plc = (PlaceholderContent) cnt;
        return plc.resolve(ctx);
    });

}
