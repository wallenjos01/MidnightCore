package org.wallentines.mcore.text;

import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.*;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Converts Component Contents into native Minecraft contents. Only default content types are implemented by default.
 * If a mod uses custom component types, logic for converting them will need to be registered here if they are to be
 * accessible from MidnightCore APIs.
 * @param <T> The type of content to convert
 */
public abstract class ContentConverter<T extends Content, M extends ComponentContents> {

    private final Class<T> clazz;
    private final Class<M> targetClazz;

    protected ContentConverter(Class<T> clazz, Class<M> targetClazz) {
        this.clazz = clazz;
        this.targetClazz = targetClazz;
    }

    /**
     * Converts a MidnightCore component Content into an instance of Minecraft ComponentContents
     * @param content The Content to convert
     * @return The converted ComponentContents
     */
    protected abstract M convert(T content);


    /**
     * Converts a Minecraft ComponentContents into a MidnightCore component Content
     * @param content The ComponentContents to convert
     * @return The converted Content
     */
    protected abstract T convertReverse(M content);

    /**
     * Attempts to convert a Content object, or throws an exception otherwise
     * @param content The content to convert
     * @return A converted ComponentContents
     * @throws IllegalStateException if the Content is not the right type
     */
    public M convertOrThrow(Content content) throws IllegalStateException {

        if(!clazz.isAssignableFrom(content.getClass())) {
            throw new IllegalStateException("Unable to convert " + content + "! Expected a " + clazz.getCanonicalName());
        }

        return convert(clazz.cast(content));
    }



    /**
     * Attempts to convert a ComponentContents object, or throws an exception otherwise
     * @param content The content to convert
     * @return A converted Content
     * @throws IllegalStateException if the ComponentContents is not the right type
     */
    public Content convertReverseOrThrow(ComponentContents content) throws IllegalStateException {

        if(!targetClazz.isAssignableFrom(content.getClass())) {
            throw new IllegalStateException("Unable to convert " + content + "! Expected a " + clazz.getCanonicalName());
        }

        return convertReverse(targetClazz.cast(content));
    }


    public static final StringRegistry<ContentConverter<?, ?>> REGISTRY = new StringRegistry<>();

    /**
     * Registers a new ContentConverter type
     * @param type The content type. (i.e. text, translate, etc.)
     * @param clazz The expected Content subclass
     * @param converter The actual converter code
     * @param <T> The type of Content to convert
     */
    public static <T extends Content, M extends ComponentContents> void register(String type, Class<T> clazz, Class<M> targetClazz, Function<T, M> converter, Function<M, T> reverse) {
        ContentConverter<T, M> out = new ContentConverter<>(clazz, targetClazz) {
            @Override
            public M convert(T content) {
                return converter.apply(content);
            }

            @Override
            protected T convertReverse(M content) {
                return reverse.apply(content);
            }
        };

        REGISTRY.register(type, out);
    }

    /**
     * Converts a Component and all of its children to a Minecraft Component
     * @param comp The Component to convert
     * @return A converted MutableComponent
     */
    public static MutableComponent convertComponent(Component comp) {

        MutableComponent out = MutableComponent.create(convertContent(comp.content));

        out.setStyle(ConversionUtil.getStyle(comp));

        for(Component child : comp.children) {
            out.append(convertComponent(child));
        }

        return out;
    }

    /**
     * Converts a Minecraft Component and all of its children to a MidnightCore Component
     * @param comp The Minecraft Component to convert
     * @return A converted MidnightCore Component
     */
    public static Component convertReverse(net.minecraft.network.chat.Component comp) {

        ContentConverter<?, ?> converter = null;
        for(ContentConverter<?, ?> conv : REGISTRY) {
            if(conv.targetClazz == comp.getContents().getClass()) {
                converter = conv;
            }
        }

        if(converter == null) {
            throw new IllegalStateException("Don't know how to convert Minecraft component type " + comp.getContents().getClass() + " into a Contents!");
        }

        Content contents = converter.convertReverseOrThrow(comp.getContents());
        Component out = new Component(contents);

        Style style = comp.getStyle();
        if(!style.isEmpty()) {

            if(style.isBold()) out = out.withBold(true);
            if(style.isItalic()) out = out.withItalic(true);
            if(style.isUnderlined()) out = out.withUnderlined(true);
            if(style.isStrikethrough()) out = out.withStrikethrough(true);
            if(style.isObfuscated()) out = out.withObfuscated(true);

            if(style.getFont() != Style.DEFAULT_FONT) out = out.withFont(ConversionUtil.toIdentifier(style.getFont()));

            out = out.withInsertion(style.getInsertion());
            if(style.getColor() != null) out = out.withColor(ConversionUtil.toColor(style.getColor()));
            if(style.getHoverEvent() != null) out = out.withHoverEvent(ConversionUtil.toHoverEvent(style.getHoverEvent()));
            if(style.getClickEvent() != null) out = out.withClickEvent(ConversionUtil.toClickEvent(style.getClickEvent()));
        }

        for(net.minecraft.network.chat.Component child : comp.getSiblings()) {
            out = out.addChild(convertReverse(child));
        }

        return out;
    }

    /**
     * Converts the given MidnightCore Content to a Minecraft ComponentContents object
     * @param content The Content to convert
     * @return A new ComponentContents
     */
    public static ComponentContents convertContent(Content content) {
        ContentConverter<?, ?> converter = REGISTRY.get(content.type);
        if(converter == null) {
            throw new IllegalStateException("Don't know how to convert content type " + content.type + " into a Minecraft component!");
        }

        return converter.convertOrThrow(content);
    }


    static {

        // Default types
        register("text", Content.Text.class, LiteralContents.class,
                (md) -> new LiteralContents(md.text),
                (mc) -> new Content.Text(mc.text()));

        register("translate", Content.Translate.class, TranslatableContents.class,
                (md) -> new TranslatableContents(
                        md.key,
                        md.fallback,
                        md.with == null ? null : md.with.stream().map(ContentConverter::convertComponent).toArray()),
                (mc) -> new Content.Translate(
                        mc.getKey(),
                        mc.getFallback(),
                        mc.getArgs().length == 0 ? null : Stream.of(mc.getArgs()).map(obj -> ContentConverter.convertReverse((net.minecraft.network.chat.Component) obj)).toList()));

        register("keybind", Content.Keybind.class, KeybindContents.class,
                (md) -> new KeybindContents(md.key),
                (mc) -> new Content.Keybind(mc.getName()));

        register("score", Content.Score.class, ScoreContents.class,
                (md) -> new ScoreContents(md.name, md.objective),
                (mc) -> new Content.Score(mc.getName(), mc.getObjective(), null));

        register("selector", Content.Selector.class, SelectorContents.class,
                (md) -> new SelectorContents(
                        md.value,
                        Optional.ofNullable(md.separator == null ? null : convertComponent(md.separator))),
                (mc) -> new Content.Selector(
                        mc.getPattern(),
                        mc.getSeparator().map(ContentConverter::convertReverse).orElse(null))
                );

        register("nbt", Content.NBT.class, NbtContents.class,
                (md) -> {
                    DataSource source = switch (md.type) {
                        case BLOCK -> new BlockDataSource(md.data);
                        case ENTITY -> new EntityDataSource(md.data);
                        default -> new StorageDataSource(new ResourceLocation(md.data));
                    };
                    return new NbtContents(
                            md.path,
                            md.interpret,
                            Optional.ofNullable(md.separator == null ? null : convertComponent(md.separator)),
                            source
                    );
                },
                (mc) -> {
                    String pattern;
                    Content.NBT.DataSourceType type;
                    if(mc.getDataSource() instanceof BlockDataSource) {
                        pattern = ((BlockDataSource) mc.getDataSource()).posPattern();
                        type = Content.NBT.DataSourceType.BLOCK;
                    }
                    else if(mc.getDataSource() instanceof EntityDataSource) {
                        pattern = ((EntityDataSource) mc.getDataSource()).selectorPattern();
                        type = Content.NBT.DataSourceType.ENTITY;
                    }
                    else {
                        pattern = ((StorageDataSource) mc.getDataSource()).id().toString();
                        type = Content.NBT.DataSourceType.STORAGE;
                    }
                    return new Content.NBT(mc.getNbtPath(), mc.isInterpreting(), mc.getSeparator().map(ContentConverter::convertReverse).orElse(null), type, pattern);
                }
        );
    }
}
