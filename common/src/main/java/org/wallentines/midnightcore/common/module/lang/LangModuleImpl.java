package org.wallentines.midnightcore.common.module.lang;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.lang.*;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.MTranslateComponent;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LangModuleImpl implements LangModule {

    private String serverLanguage;

    private final HashMap<String, PlaceholderSupplier<MComponent>> placeholders = new HashMap<>();
    private final HashMap<String, PlaceholderSupplier<String>> inlinePlaceholders = new HashMap<>();

    protected LangModuleImpl() { }

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

    private static int readUntil(char chara, int offset, char[] buffer, StringBuilder out) {

        int i;
        for(i = offset ; i < buffer.length ; i++) {

            if(buffer[i] == chara) return i;
            out.append(buffer[i]);

        }
        return i;
    }

    private PlaceholderSupplier.PlaceholderContext createContext(String placeholder, Object... args) {
        StringBuilder name = new StringBuilder();
        String parameter = null;

        char[] arr = placeholder.toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            i = readUntil('<', i, arr, name);
            if(i < arr.length) {

                StringBuilder param = new StringBuilder();
                i = readUntil('>', ++i, arr, param);

                parameter = param.toString();
            }
        }
        return new PlaceholderSupplier.PlaceholderContext(name.toString(), args, parameter);
    }

    private String parsePlaceholderInline(String placeholder, Object... args) {

        PlaceholderSupplier.PlaceholderContext ctx = createContext(placeholder, args);

        PlaceholderSupplier<String> pl = inlinePlaceholders.get(ctx.getName());
        if(pl == null) for(Object o : args) {
            if(o instanceof CustomPlaceholderInline ci) {
                if(ci.getId().equals(placeholder)) pl = ci;
            }
        }

        String s = PlaceholderSupplier.get(pl, ctx);
        return s == null ? ctx.toRawPlaceholder() : s;
    }

    private MComponent parsePlaceholder(String placeholder, Object... args) {

        PlaceholderSupplier.PlaceholderContext ctx = createContext(placeholder, args);

        PlaceholderSupplier<MComponent> pl = placeholders.get(ctx.getName());
        if(pl == null) for(Object o : args) {
            if(o instanceof CustomPlaceholder ci) {
                if(ci.getId().equals(placeholder)) pl = ci;
            }
        }

        MComponent cmp = PlaceholderSupplier.get(pl, ctx);
        return cmp == null ? new MTextComponent(ctx.toRawPlaceholder()) : cmp;
    }

    @Override
    public String applyInlinePlaceholders(String input, Object... args) {
        StringBuilder out = new StringBuilder();

        char[] arr = input.toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            i = readUntil('%', i, arr, out);
            if(i < arr.length) {

                StringBuilder placeholder = new StringBuilder();
                i = readUntil('%', ++i, arr, placeholder);

                out.append(i == arr.length ? "%" + placeholder : parsePlaceholderInline(placeholder.toString(), args));
            }
        }

        return out.toString();
    }

    @Override
    public MComponent applyPlaceholders(MComponent input, Object... args) {

        MStyle style = input.getStyle();
        List<MComponent> components = new ArrayList<>();

        char[] arr = input.getContent().toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            StringBuilder current = new StringBuilder();
            i = readUntil('%', i, arr, current);
            components.add(new MTextComponent(current.toString()));

            if(i < arr.length) {

                StringBuilder placeholder = new StringBuilder();
                i = readUntil('%', ++i, arr, placeholder);

                MComponent pl = i == arr.length ? new MTextComponent("%" + placeholder) : parsePlaceholder(placeholder.toString(), args);
                components.add(pl);
            }
        }

        MComponent out = components.isEmpty() ? new MTextComponent("") : components.get(0);
        out.getStyle().fillFrom(style);

        for(int i = 1 ; i < components.size() ; i++) {
            out.addChild(components.get(i));
        }

        for(MComponent comp : input.getChildren()) {
            out.addChild(applyPlaceholders(comp, args));
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
    public String getInlinePlaceholderValue(String key, String parameter, Object... args) {

        for(Object o : args) {
            if(o instanceof CustomPlaceholderInline cpi) {
                if(cpi.getId().equals(key)) return cpi.get();
            }
        }

        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(key, args, parameter);
        return PlaceholderSupplier.get(inlinePlaceholders.get(key), ctx);
    }

    @Override
    public MComponent getPlaceholderValue(String key, String parameter, Object... args) {

        for(Object o : args) {
            if(o instanceof CustomPlaceholder cp) {
                if(cp.getId().equals(key)) return cp.get();
            }
        }

        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(key, args, parameter);
        return PlaceholderSupplier.get(placeholders.get(key), ctx);
    }

    @Override
    public String getServerLanguage() {
        return serverLanguage;
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        reload(section);

        registerPlaceholder("player_name", PlaceholderSupplier.create(MPlayer.class, MPlayer::getName));
        registerInlinePlaceholder("player_uuid", PlaceholderSupplier.create(MPlayer.class, mp -> mp.getUUID().toString()));

        registerInlinePlaceholder(Constants.DEFAULT_NAMESPACE + "_version", PlaceholderSupplier.create(Constants.VERSION.toString()));
        registerInlinePlaceholder("server_version", PlaceholderSupplier.create(MidnightCoreAPI.getInstance().getGameVersion().toString()));

        registerPlaceholder("lang", ctx -> {

            String param = ctx.getParameter();
            if(param == null) return null;

            MPlayer mpl = ctx.getArgument(MPlayer.class);
            LangProvider provider = ctx.getArgument(LangProvider.class);

            if(provider == null) return new MTextComponent(param);

            MComponent msg = provider.getMessage(param, mpl, ctx.getArgs());
            return msg == null ? new MTextComponent(param) : msg;
        });

        registerPlaceholder("translate", PlaceholderSupplier.createWithParameter(MTranslateComponent::new));

        return true;
    }

    @Override
    public void reload(ConfigSection config) {

        serverLanguage = config.getString("locale");
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "lang");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(LangModuleImpl::new, ID, new ConfigSection().with("locale", "en_us"));
}
