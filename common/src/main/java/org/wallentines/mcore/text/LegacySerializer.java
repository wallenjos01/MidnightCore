package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.RegistryBase;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * A {@link Serializer} which serializes components into legacy Strings for clients/contexts which do not
 * support modern text components
 */
public class LegacySerializer implements Serializer<Component> {


    /**
     * Contains serializers for the default content types
     */
    public static final StringRegistry<InlineContentSerializer<?>> CONTENT_SERIALIZERS = new StringRegistry<>();

    /**
     * A LegacySerializer instance which uses the color code character and does not support hex. For true legacy text
     */
    public static final LegacySerializer INSTANCE = new LegacySerializer('\u00A7', false, CONTENT_SERIALIZERS);

    /**
     * A LegacySerializer instance which uses an ampersand for color codes and has hex support. For text stored in
     * config files.
     */
    public static final LegacySerializer CONFIG_INSTANCE = new LegacySerializer('&', true, CONTENT_SERIALIZERS);

    private final Character colorChar;
    private final boolean hexSupport;
    private final RegistryBase<String, InlineContentSerializer<?>> serializers;

    /**
     * Creates a LegacySerializer with the given color character, and optional rgb color support
     * @param colorChar The character which should prefix all color codes
     * @param hexSupport Whether hex color codes which appear after the color character (i.e. &#112233 if the color
     *                   character is '&') should be parsed or written.
     * @param serializers The content serializers to use
     */
    public LegacySerializer(Character colorChar, boolean hexSupport, RegistryBase<String, InlineContentSerializer<?>> serializers) {
        this.colorChar = colorChar;
        this.hexSupport = hexSupport;
        this.serializers = serializers;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {

        SerializeResult<String> content = serializeContent(context, value.content).map(o -> {
            if(!context.isString(o)) {
                return SerializeResult.failure("Unable to serialize component content! Expected string!");
            }
            return SerializeResult.success(context.asString(o));
        });

        if(!content.isComplete()) {
            return SerializeResult.failure(content.getError());
        }

        String style = toLegacyStyle(value);
        StringBuilder out = new StringBuilder(style);
        out.append(content.getOrThrow());

        for(Component child : value.children) {

            SerializeResult<O> childRes = serialize(context, child);
            if(!childRes.isComplete()) {
                return childRes;
            }

            out.append(context.asString(childRes.getOrThrow()));
        }

        return SerializeResult.success(context.toString(out.toString()));
    }

    private <O> SerializeResult<O> serializeContent(SerializeContext<O> context, Content value) {

        InlineContentSerializer<?> ser = serializers.get(value.type);
        if(ser == null) {
            return SerializeResult.failure("Serializer for component contents with type " + value.type + " not found!");
        }

        return ser.serialize(context, value);
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

        if(!context.isString(value)) {
            return SerializeResult.failure("Unable to deserialize " + value + "! Expected a String!");
        }

        return SerializeResult.success(parsePlainText(context.asString(value)));
    }


    private String toLegacyStyle(Component component) {

        StringBuilder out = new StringBuilder();

        if(component.color != null) out.append(colorChar).append(hexSupport ?
                                    '#' + component.color.toPlainHex() :
                                    Integer.toHexString(component.color.toRGBI()));

        if(component.bold != null && component.bold)                   out.append(colorChar).append("l");
        if(component.italic  != null && component.italic)              out.append(colorChar).append("o");
        if(component.underlined != null && component.underlined)       out.append(colorChar).append("n");
        if(component.strikethrough != null && component.strikethrough) out.append(colorChar).append("m");
        if(component.obfuscated != null && component.obfuscated)       out.append(colorChar).append("k");

        return out.toString();
    }


    private Component parsePlainText(String content) {

        List<Component> out = new ArrayList<>();

        StringBuilder currentString = new StringBuilder();
        Component currentComponent = Component.empty();

        for(int i = 0 ; i < content.length() ; i++) {

            char c = content.charAt(i);
            if(c == colorChar && i < content.length() - 1) {
                char next = content.charAt(i + 1);

                if(next == colorChar) {
                    currentString.append(colorChar);
                    i += 1;
                }
                else if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {

                    int legacy = Integer.parseInt(String.valueOf(next), 16);
                    if(!currentString.isEmpty()) {
                        out.add(currentComponent.withContent(new Content.Text(currentString.toString())));
                    }
                    currentString = new StringBuilder();

                    i += 1;
                    currentComponent = Component.empty().withColor(Color.fromRGBI(legacy));

                } else if(next == 'r') {

                    if(!currentString.isEmpty()) {
                        out.add(currentComponent.withContent(new Content.Text(currentString.toString())));
                    }
                    currentString = new StringBuilder();
                    i += 1;

                    currentComponent = Component.empty().withReset(true);

                } else if(hexSupport && next == '#' && i < content.length() - 8) {

                    String hex = content.substring(i + 2, i + 8);
                    out.add(currentComponent.withContent(new Content.Text(currentString.toString())));

                    currentString = new StringBuilder();

                    i += 7;
                    currentComponent = Component.empty().withColor(Color.parse(hex).getOrThrow());

                } else {
                    switch (next) {
                        case 'l': { currentComponent = currentComponent.withBold(true); i += 1; break; }
                        case 'o': { currentComponent = currentComponent.withItalic(true); i += 1; break; }
                        case 'n': { currentComponent = currentComponent.withUnderlined(true); i += 1; break; }
                        case 'm': { currentComponent = currentComponent.withStrikethrough(true); i += 1; break; }
                        case 'k': { currentComponent = currentComponent.withObfuscated(true); i += 1; break; }
                    }
                }
            } else {
                currentString.append(c);
            }
        }

        out.add(currentComponent.withContent(new Content.Text(currentString.toString())));

        int i = 0;
        Component outComp = out.get(0).hasFormatting() ? Component.text("") : out.get(i++);
        for(; i < out.size() ; i++) {
            outComp = outComp.addChild(out.get(i));
        }

        return outComp;
    }

    private static <T extends Content> void register(String id, Class<T> clazz, Function<T, String> ser) {
        InlineContentSerializer<T> out = new InlineContentSerializer<>(clazz, InlineSerializer.of(ser, str -> {
            throw new IllegalStateException("Cannot deserialize component content from legacy text!");
        }));
        CONTENT_SERIALIZERS.register(id, out);
    }

    static {

        register("text", Content.Text.class, txt -> txt.text);
        register("translate", Content.Translate.class, tra -> tra.key);
        register("keybind", Content.Keybind.class, key -> key.key);
        register("score", Content.Score.class, score -> "");
        register("selector", Content.Selector.class, sel -> "");
        register("nbt", Content.NBT.class, nbt -> "");
    }

}
