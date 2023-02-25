package org.wallentines.midnightcore.fabric.util;

import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightcore.fabric.mixin.AccessorStyle;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConversionUtil {

    public static Identifier toIdentifier(ResourceLocation loc) {
        if(loc == null) return null;
        return new Identifier(loc.getNamespace(), loc.getPath());
    }

    public static ResourceLocation toResourceLocation(Identifier id) {
        if(id == null) return null;
        return new ResourceLocation(id.getNamespace(), id.getPath());
    }

    public static ConfigSection toConfigSection(CompoundTag tag) {

        if(tag == null) return null;
        NBTContext ctx = new NBTContext();
        return ctx.convert(ConfigContext.INSTANCE, tag).asSection();
    }

    public static CompoundTag toCompoundTag(ConfigSection section) {

        if(section == null) return null;
        NBTContext ctx = new NBTContext(true);
        return (CompoundTag) ConfigContext.INSTANCE.convert(ctx, section);
    }

    public static MComponent toMComponent(@Nullable Component component) {

        if(component == null) return null;

        MComponent out;

        ComponentContents contents = component.getContents();

        if(contents instanceof TranslatableContents) {

            List<MComponent> comps = new ArrayList<>();
            for(Object o : ((TranslatableContents) contents).getArgs()) {
                comps.add(toMComponent((Component) o));
            }

            out = new MTranslateComponent(((TranslatableContents) contents).getKey(), comps.isEmpty() ? null : comps);

        } else if(contents instanceof LiteralContents) {

            out = new MTextComponent(((LiteralContents) contents).text());

        } else {

            return MComponent.SERIALIZER.deserialize(GsonContext.INSTANCE, Component.Serializer.toJsonTree(component)).getOrThrow();
        }

        out.withStyle(toMStyle(component.getStyle()));
        out.setHoverEvent(toMHoverEvent(component.getStyle().getHoverEvent()));
        out.setClickEvent(toMClickEvent(component.getStyle().getClickEvent()));

        for(Component cmp : component.getSiblings()) {
            out.addChild(toMComponent(cmp));
        }
        return out;
    }

    public static MStyle toMStyle(Style style) {

        MStyle out = new MStyle();
        if(style.isEmpty()) return out;

        AccessorStyle acc = (AccessorStyle) style;

        if(style.getColor() != null) out.withColor(TextColor.parse(style.getColor().serialize()));
        out.withBold(acc.getBold());
        out.withItalic(acc.getItalic());
        out.withUnderlined(acc.getUnderlined());
        out.withStrikethrough(acc.getStrikethrough());
        out.withObfuscated(acc.getObfuscated());

        out.withFont(toIdentifier(style.getFont()));

        return out;
    }

    public static MHoverEvent toMHoverEvent(HoverEvent event) {

        if(event == null) return null;

        String json = event.serialize().toString();
        ConfigObject obj = JSONCodec.minified().decode(ConfigContext.INSTANCE, json);

        return MHoverEvent.SERIALIZER.deserialize(ConfigContext.INSTANCE, obj).getOrThrow();
    }

    public static MClickEvent toMClickEvent(ClickEvent event) {

        if(event == null) return null;
        return new MClickEvent(MClickEvent.ClickAction.byId(event.getAction().getName()), event.getValue());
    }

    public static Component toComponent(@Nullable MComponent component) {

        if(component == null) return null;

        MutableComponent out;
        if(component instanceof MTextComponent) {

            out = Component.literal(component.getContent());

        } else if(component instanceof MTranslateComponent) {

            Collection<MComponent> args = ((MTranslateComponent) component).getArgs();
            if(args == null) {
                out = Component.translatable(component.getContent());
            } else {

                Object[] comps = new Component[args.size()];

                int i = 0;
                for (MComponent arg : args) {
                    comps[i++] = toComponent(arg);
                }

                out = Component.translatable(component.getContent(), comps);
            }

        } else {

            return Component.Serializer.fromJson(JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, MComponent.SERIALIZER.serialize(ConfigContext.INSTANCE, component).getOrThrow()));
        }

        out.setStyle(toStyle(component.getStyle())
                .withInsertion(component.getInsertion())
                .withHoverEvent(toHoverEvent(component.getHoverEvent()))
                .withClickEvent(toClickEvent(component.getClickEvent()))
        );

        for(MComponent child : component.getChildren()) {
            out.append(toComponent(child));
        }

        return out;
    }

    public static Style toStyle(MStyle style) {

        if(style == null) return Style.EMPTY;
        Color color = style.getColor();

        return Style.EMPTY
                .withColor(color == null ? null : net.minecraft.network.chat.TextColor.fromRgb(color.toDecimal()))
                .withBold(style.getBold())
                .withItalic(style.getItalic())
                .withUnderlined(style.getUnderlined())
                .withStrikethrough(style.getStrikethrough())
                .withObfuscated(style.getObfuscated())
                .withFont(toResourceLocation(style.getFont()));
    }

    public static HoverEvent toHoverEvent(MHoverEvent event) {

        if(event == null) return null;
        return HoverEvent.deserialize(MHoverEvent.SERIALIZER.serialize(GsonContext.INSTANCE, event).getOrThrow().getAsJsonObject());
    }

    public static ClickEvent toClickEvent(MClickEvent event) {

        if(event == null) return null;
        return new ClickEvent(ClickEvent.Action.getByName(event.getAction().getId()), event.getValue());
    }

}
