package org.wallentines.midnightcore.common.module.lang;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.lang.*;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.util.HashMap;

public class LangModuleImpl implements LangModule {

    private String serverLanguage;

    private final HashMap<String, PlaceholderSupplier<MComponent>> placeholders = new HashMap<>();
    private final HashMap<String, PlaceholderSupplier<String>> inlinePlaceholders = new HashMap<>();

    protected LangModuleImpl() {

        registerPlaceholder("player_name", PlaceholderSupplier.create(MPlayer.class, MPlayer::getName));
        registerInlinePlaceholder("player_uuid", PlaceholderSupplier.create(MPlayer.class, mp -> mp.getUUID().toString()));

    }

    @Override
    public LangProvider createProvider(Path folderPath, ConfigSection defaults) {
        return new LangProviderImpl(folderPath.toAbsolutePath(), this, defaults);
    }

    @Override
    public void registerPlaceholder(String key, PlaceholderSupplier<MComponent> supplier) {
        placeholders.put(key, supplier);
    }

    @Override
    public void registerInlinePlaceholder(String key, PlaceholderSupplier<String> supplier) {
        inlinePlaceholders.put(key, supplier);
    }

    public String applyInlinePlaceholders(String msg, Object... data) {

        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder message = new StringBuilder();

        for(int i = 0 ; i < msg.length() ; i++) {

            char c = msg.charAt(i);
            if(c == '%') {

                if(placeholder) {
                    String rep = getInlinePlaceholderValue(currentPlaceholder.toString(), data);
                    message.append(rep == null ? "%" + currentPlaceholder + "%" : rep);
                    currentPlaceholder = new StringBuilder();
                }
                placeholder = !placeholder;

            } else {
                if(placeholder) {
                    currentPlaceholder.append(c);
                } else {
                    message.append(c);
                }
            }
        }

        if(currentPlaceholder.length() > 0) message.append("%").append(currentPlaceholder);
        return message.toString();

    }

    @Override
    public MComponent applyPlaceholders(MComponent msg, Object... data) {

        MStyle style = msg.getStyle();
        MComponent out = new MTextComponent("").withStyle(style);
        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder currentMessage = new StringBuilder();

        for(int i = 0 ; i < msg.getContent().length() ; i++) {

            char c = msg.getContent().charAt(i);

            if(c == '%') {

                if(placeholder) {

                    MComponent rep = getPlaceholderValue(currentPlaceholder.toString(), data);
                    if(rep == null) {
                        currentMessage.append("%").append(currentPlaceholder).append("%");

                    } else {
                        out.addChild(new MTextComponent(currentMessage.toString()));
                        out.addChild(rep);
                        currentMessage = new StringBuilder();
                    }
                    currentPlaceholder = new StringBuilder();
                }
                placeholder = !placeholder;

            } else {
                if(placeholder) {
                    currentPlaceholder.append(c);
                } else {
                    currentMessage.append(c);
                }
            }
        }

        if(currentPlaceholder.length() > 0) currentMessage.append("%").append(currentPlaceholder);
        out.addChild(new MTextComponent(currentMessage.toString()));

        for(MComponent comp : msg.getChildren()) {
            out.addChild(applyPlaceholders(comp, data));
        }

        return out;
    }

    @Override
    public MComponent parseText(String text, Object... data) {

        text = applyInlinePlaceholders(text, data);
        MComponent comp = MComponent.parse(text);
        return applyPlaceholders(comp, data);
    }

    @Override
    public String getInlinePlaceholderValue(String key, Object... data) {

        for(Object o : data) {
            if(o instanceof CustomPlaceholderInline) {
                CustomPlaceholderInline cpi = (CustomPlaceholderInline) o;
                if(cpi.getId().equals(key)) return cpi.get();
            }
        }
        return inlinePlaceholders.containsKey(key) ? inlinePlaceholders.get(key).get(data) : null;
    }

    @Override
    public MComponent getPlaceholderValue(String key, Object... data) {

        for(Object o : data) {
            if(o instanceof CustomPlaceholder) {
                CustomPlaceholder cp = (CustomPlaceholder) o;
                if(cp.getId().equals(key)) return cp.get();
            }
        }
        return placeholders.containsKey(key) ? placeholders.get(key).get(data) : null;
    }

    @Override
    public String getServerLanguage() {
        return serverLanguage;
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        reload(section);

        registerInlinePlaceholder("midnightcore_version", PlaceholderSupplier.create(Constants.VERSION.toString()));
        registerInlinePlaceholder("server_version", PlaceholderSupplier.create(MidnightCoreAPI.getInstance().getGameVersion().toString()));

        return true;
    }

    @Override
    public void reload(ConfigSection config) {

        serverLanguage = config.getString("locale");

    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "lang");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(LangModuleImpl::new, ID, new ConfigSection().with("locale", "en_us"));
}
