package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.text.Content;

import java.util.Collection;
import java.util.List;

public class LangContent extends Content {


    private final LangManager manager;
    private final String key;
    private final Collection<Object> context;

    public LangContent(LangManager manager, String key, Collection<Object> context) {
        super("lang");
        this.manager = manager;
        this.key = key;
        this.context = context;
    }

    public LangManager getLangManager() {
        return manager;
    }

    public String getKey() {
        return key;
    }

    public Component resolve(Object... args) {

        PlaceholderContext ctx;
        if(args == null) {
            ctx = new PlaceholderContext();
        } else {
            ctx = new PlaceholderContext(List.of(args));
        }

        for(Object o : context) {
            ctx.addValue(o);
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
