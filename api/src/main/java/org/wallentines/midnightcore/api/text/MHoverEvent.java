package org.wallentines.midnightcore.api.text;


import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public class MHoverEvent {

    private final HoverAction action;
    private final ConfigSection data;

    private MHoverEvent(HoverAction action, ConfigSection data) {

        this.action = action;
        this.data = data;
    }

    public HoverAction getAction() {
        return action;
    }

    public ConfigSection getContents() {

        return data;
    }

    public static MHoverEvent createTextHover(MComponent hover) {
        return createTextHover(hover, true);
    }

    public static MHoverEvent createTextHover(MComponent hover, boolean rgb) {

        return new MHoverEvent(HoverAction.SHOW_TEXT, MComponent.SERIALIZER.serialize(hover));
    }

    public static MHoverEvent createItemHover(MItemStack stack) {

        return new MHoverEvent(HoverAction.SHOW_ITEM, new ConfigSection()
                .with("id", stack.getType().toString())
                .with("Count", stack.getCount())
                .with("tag", stack.getTag() == null ? null : stack.getTag().toString()));
    }

    public static MHoverEvent createEntityHover(Identifier entityType, UUID uid, MComponent name) {

        return new MHoverEvent(HoverAction.SHOW_ENTITY, new ConfigSection()
                .with("type", entityType)
                .with("id", uid)
                .with("name", name));
    }

    public static MHoverEvent createPlayerHover(MPlayer player) {

        return new MHoverEvent(HoverAction.SHOW_ENTITY, new ConfigSection()
                .with("type", "minecraft:player")
                .with("id", player.getUUID().toString())
                .with("name", MComponent.SERIALIZER.serialize(player.getName())));
    }

    public static final ConfigSerializer<MHoverEvent> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.STRING.entry("action", ev -> ev.action.getId()),
            ConfigSerializer.RAW.entry("contents", ev -> ev.data),
            (action, contents) -> new MHoverEvent(HoverAction.getById(action), contents));

    public enum HoverAction {

        SHOW_TEXT("show_text"),
        SHOW_ITEM("show_item"),
        SHOW_ENTITY("show_entity");

        final String id;

        HoverAction(String id) {
            this.id = id;
        }

        public static HoverAction getById(String id) {
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
