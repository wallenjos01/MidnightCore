package org.wallentines.midnightcore.api.text;


import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public class MHoverEvent {

    private final HoverAction action;
    private final ConfigObject data;

    public MHoverEvent(HoverAction action, ConfigObject data) {

        this.action = action;
        this.data = data;
    }

    public HoverAction getAction() {
        return action;
    }

    public ConfigObject getContents() {

        return data;
    }

    public MComponent getContentsAsText() {

        return MComponent.SERIALIZER.deserialize(ConfigContext.INSTANCE, data).getOrThrow();
    }

    public static MHoverEvent createTextHover(MComponent hover) {

        return new MHoverEvent(HoverAction.SHOW_TEXT, MComponent.OBJECT_SERIALIZER.serialize(ConfigContext.INSTANCE, hover).getOrThrow());
    }

    public static MHoverEvent createItemHover(MItemStack stack) {

        return new MHoverEvent(HoverAction.SHOW_ITEM, new ConfigSection()
                .with("id", stack.getType().toString())
                .with("Count", stack.getCount())
                .with("tag", stack.getTag() == null ? null : JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, stack.getTag())));
    }

    public static MHoverEvent createEntityHover(Identifier entityType, UUID uid, MComponent name) {

        return new MHoverEvent(HoverAction.SHOW_ENTITY, new ConfigSection()
                .with("type", entityType.toString())
                .with("id", uid.toString())
                .with("name", name.toJSONString()));
    }

    public static MHoverEvent createPlayerHover(MPlayer player) {

        return new MHoverEvent(HoverAction.SHOW_ENTITY, new ConfigSection()
                .with("type", "minecraft:player")
                .with("id", player.getUUID().toString())
                .with("name", player.getName().toJSONString()));
    }

    public static final Serializer<MHoverEvent> SERIALIZER = ObjectSerializer.create(
            InlineSerializer.of(HoverAction::getId, HoverAction::byId).entry("action", MHoverEvent::getAction),
            ConfigObject.SERIALIZER.entry("contents", ev -> ev.data),
            MHoverEvent::new);

    public enum HoverAction {

        SHOW_TEXT("show_text"),
        SHOW_ITEM("show_item"),
        SHOW_ENTITY("show_entity");

        final String id;

        HoverAction(String id) {
            this.id = id;
        }

        public static HoverAction byId(String id) {
            for(HoverAction act : values()) {
                if(act.id.equals(id)) return act;
            }
            return null;
        }

        public String getId() {
            return id;
        }
    }

}
