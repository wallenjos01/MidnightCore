package me.m1dnightninja.midnightcore.common.module.lang;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholder;
import me.m1dnightninja.midnightcore.api.module.lang.CustomPlaceholderInline;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;

import java.util.HashMap;

public abstract class AbstractLangModule implements ILangModule {

    private static final MIdentifier ID = MIdentifier.create("midnightcore", "lang");

    private final HashMap<String, PlaceholderSupplier<String>> inlineSuppliers = new HashMap<>();
    private final HashMap<String, PlaceholderSupplier<MComponent>> suppliers = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        registerPlaceholderSupplier("player_name", PlaceholderSupplier.create(MPlayer.class, MPlayer::getName));
        registerPlaceholderSupplier("player_display_name", PlaceholderSupplier.create(MPlayer.class, MPlayer::getDisplayName));
        registerInlinePlaceholderSupplier("player_uuid", PlaceholderSupplier.create(MPlayer.class, pl -> pl.getUUID().toString()));

        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }


    @Override
    public String getServerLanguage() {
        return MidnightCoreAPI.getInstance().getMainConfig().has("language") ? MidnightCoreAPI.getInstance().getMainConfig().getString("language") : "en_us";
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return new ConfigSection();
    }

    @Override
    public void registerInlinePlaceholderSupplier(String search, PlaceholderSupplier<String> supplier) {
        inlineSuppliers.put(search, supplier);
    }

    @Override
    public void registerPlaceholderSupplier(String search, PlaceholderSupplier<MComponent> supplier) {
        suppliers.put(search, supplier);
    }

    @Override
    public MComponent getPlaceholderValue(String key, Object... args) {

        PlaceholderSupplier<MComponent> supplier = suppliers.get(key);
        if(supplier == null) {
            for(Object o : args) {
                if(o instanceof CustomPlaceholder) {
                    CustomPlaceholder pl = (CustomPlaceholder) o;
                    if(pl.getKey().equals(key)) {
                        return pl.getReplacement();
                    }
                }
            }
            return null;
        }

        return supplier.get(args);
    }

    @Override
    public String getInlinePlaceholderValue(String key, Object... args) {

        PlaceholderSupplier<String> supplier = inlineSuppliers.get(key);
        if(supplier == null) {
            for(Object o : args) {
                if(o instanceof CustomPlaceholderInline) {
                    CustomPlaceholderInline pl = (CustomPlaceholderInline) o;
                    if(pl.getKey().equals(key)) {
                        return pl.getReplacement();
                    }
                }
            }
            return null;
        }

        return supplier.get(args);

    }

    @Override
    public String applyInlinePlaceholders(String msg, Object... args) {

        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder message = new StringBuilder();

        for(int i = 0 ; i < msg.length() ; i++) {

            char c = msg.charAt(i);
            if(c == '%') {

                if(placeholder) {

                    String rep = getInlinePlaceholderValue(currentPlaceholder.toString(), args);
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
    public MComponent applyPlaceholders(MComponent msg, Object... args) {

        MStyle style = msg.getStyle();
        MComponent out = MComponent.createTextComponent("").withStyle(style);

        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder currentMessage = new StringBuilder();

        for(int i = 0 ; i < msg.getContent().length() ; i++) {

            char c = msg.getContent().charAt(i);

            if(c == '%') {

                if(placeholder) {

                    MComponent rep = getPlaceholderValue(currentPlaceholder.toString(), args);
                    if(rep == null) {
                        currentMessage.append("%").append(currentPlaceholder).append("%");

                    } else {

                        out.addChild(MComponent.createTextComponent(currentMessage.toString()));
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
        out.addChild(MComponent.createTextComponent(currentMessage.toString()));

        for(MComponent comp : msg.getChildren()) {
            out.addChild(applyPlaceholders(comp, args));
        }

        return out;
    }

    @Override
    public MComponent parseText(String input, Object... args) {

        input = applyInlinePlaceholders(input, args);
        MComponent out = MComponent.Serializer.parse(input);
        out = applyPlaceholders(out, args);

        return out;
    }

    @Override
    public String applyPlaceholdersFlattened(String msg, Object... args) {

        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder message = new StringBuilder();

        for(int i = 0 ; i < msg.length() ; i++) {

            char c = msg.charAt(i);
            if(c == '%') {

                if(placeholder) {

                    String rep = getInlinePlaceholderValue(currentPlaceholder.toString(), args);
                    if(rep == null) {
                        rep = getPlaceholderValue(currentPlaceholder.toString(), args).allContent();
                    }

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
}
