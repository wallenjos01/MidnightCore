package me.m1dnightninja.midnightcore.api.text;

import com.google.gson.JsonObject;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

import java.util.UUID;

public class MHoverEvent {


    private final HoverAction action;
    private final JsonObject data;

    private MHoverEvent(HoverAction action, JsonObject data) {

        this.action = action;
        this.data = data;
    }

    public HoverAction getAction() {
        return action;
    }

    public JsonObject getContents() {

        return data;
    }


    public static MHoverEvent createTextHover(MComponent hover) {
        return createTextHover(hover, true);
    }

    public static MHoverEvent createTextHover(MComponent hover, boolean rgb) {

        return new MHoverEvent(HoverAction.SHOW_TEXT, MComponent.Serializer.toJson(hover, rgb));
    }

    public static MHoverEvent createItemHover(MItemStack stack) {

        JsonObject out = new JsonObject();
        out.addProperty("id", stack.getType().toString());
        out.addProperty("Count", stack.getCount());
        out.addProperty("tag", stack.getTag().toJson().toString());

        return new MHoverEvent(HoverAction.SHOW_ITEM, out);
    }

    public static MHoverEvent createEntityHover(MIdentifier entityType, UUID uid, MComponent name) {

        JsonObject data = new JsonObject();
        data.addProperty("type", entityType.toString());
        data.addProperty("id", uid.toString());
        data.addProperty("name", MComponent.Serializer.toJsonString(name));

        return new MHoverEvent(HoverAction.SHOW_ENTITY, data);
    }

    public static MHoverEvent createPlayerHover(MPlayer player) {

        JsonObject data = new JsonObject();
        data.addProperty("type", "minecraft:player");
        data.addProperty("id", player.getUUID().toString());
        data.add("name", MComponent.Serializer.toJson(player.getName(), MComponent.Serializer.rgbSupported()));

        return new MHoverEvent(HoverAction.SHOW_ENTITY, data);
    }

    public static MHoverEvent fromJson(JsonObject obj) {

        if(!obj.has("action") || !obj.has("contents") || !obj.get("contents").isJsonObject()) return null;

        String action = obj.get("action").getAsString();
        JsonObject data = obj.get("contents").getAsJsonObject();

        HoverAction act = HoverAction.getById(action);
        if(act == null) return null;

        return new MHoverEvent(act, data);
    }

    public JsonObject toJson() {

        JsonObject obj = new JsonObject();
        obj.addProperty("action", action.getId());
        obj.add("contents", data);

        return obj;
    }

    public enum HoverAction {

        SHOW_TEXT("show_text"),
        SHOW_ITEM("show_item"),
        SHOW_ENTITY("show_entity");

        String id;

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
