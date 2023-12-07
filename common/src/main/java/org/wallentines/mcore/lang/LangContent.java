package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.text.Content;

import java.util.Collection;

/**
 * A special Component content type which resolves to a lang entry
 */
public class LangContent extends Content {


    private final LangManager manager;
    private final String key;
    private final Collection<Object> context;

    /**
     * Creates a new lang content with the given manager, key, and context
     * @param manager The manager in which to look up the key
     * @param key The lang key to look up
     * @param context Additional context by which to resolve the content
     */
    public LangContent(LangManager manager, String key, Collection<Object> context) {
        super("lang");
        this.manager = manager;
        this.key = key;
        this.context = context;
    }

    /**
     * Gets the lang manager which will be looked into during resolution
     * @return The lang manager
     */
    public LangManager getLangManager() {
        return manager;
    }

    /**
     * Gets the key which will be looked up during resolution
     * @return The lang entry key
     */
    public String getKey() {
        return key;
    }

    /**
     * Resolves the component according to the given arguments
     * @param args The arguments to pass. Will be added to the placeholder context. If this contains a LocaleHolder
     *             Object, it will be used to determine the language to lookup
     * @return A resolved Component
     */
    public Component resolve(Object... args) {

        PlaceholderContext ctx = new PlaceholderContext(context);

        if(args != null) {
            for(Object o : args) {
                ctx.addValue(o);
            }
        }

        String language = null;
        LocaleHolder holder = ctx.getValue(LocaleHolder.class);
        if(holder != null) {
            language = holder.getLanguage();
        }

        return ComponentResolver.resolveComponent(manager.getMessage(key, language, ctx), args);
    }

    @Override
    public boolean requiresResolution() {
        return true;
    }

    /**
     * Registers a default placeholder 'lang' to the given placeholder manager
     * @param manager The placeholder manager to register to.
     */
    public static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("lang", PlaceholderSupplier.of(ctx -> {

            if(ctx.getParameter() == null) {
                return null;
            }

            String param = ctx.getParameter().allText();
            LangManager langManager = ctx.getValue(LangManager.class);

            if(param == null || langManager == null) {
                return null;
            }

            return langManager.component(param);
        }));

    }
}
