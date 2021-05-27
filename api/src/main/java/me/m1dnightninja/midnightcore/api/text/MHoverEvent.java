package me.m1dnightninja.midnightcore.api.text;

import com.google.gson.JsonObject;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;

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

        return new MHoverEvent(HoverAction.SHOW_TEXT, MComponent.Serializer.toJson(hover));
    }

    public static MHoverEvent createItemHover(MItemStack stack) {

        ConfigSection out = new ConfigSection();
        out.set("id", stack.getType().toString());
        out.set("Count", stack.getCount());
        out.set("tag", stack.getTag().toJson().toString());

        return new MHoverEvent(HoverAction.SHOW_ITEM, out.toJson());
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
        obj.addProperty("action", action.getName());
        obj.add("contents", data);

        return obj;
    }

    public enum HoverAction {

        SHOW_TEXT("show_text"),
        SHOW_ITEM("show_item");

        String message;

        HoverAction(String message) {
            this.message = message;
        }

        public static HoverAction getById(String id) {
            for(HoverAction act : values()) {
                if(act.message.equals(id)) return act;
            }
            return null;
        }

        public String getName() {
            return message;
        }
    }

}
