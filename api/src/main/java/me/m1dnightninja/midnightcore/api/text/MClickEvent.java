package me.m1dnightninja.midnightcore.api.text;

import com.google.gson.JsonObject;

public class MClickEvent {

    private final ClickAction action;
    private final String value;

    public MClickEvent(ClickAction action, String value) {
        this.action = action;
        this.value = value;
    }

    public ClickAction getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    public String toString() {

        return "{\"action\":\"" + action.getId() + "\",\"value\":" + value + "\"}";
    }

    public static MClickEvent fromJson(JsonObject obj) {

        if(!obj.has("action") || !obj.has("value")) return null;

        String action = obj.get("action").getAsString();
        String value = obj.get("value").getAsString();

        ClickAction act = ClickAction.getById(action);
        if(act == null) return null;

        return new MClickEvent(act, value);
    }

    public JsonObject toJson() {

        JsonObject obj = new JsonObject();
        obj.addProperty("action", action.getId());
        obj.addProperty("value", value);

        return obj;
    }

    public enum ClickAction {

        OPEN_URL("open_url"),
        RUN_COMMAND("run_command"),
        SUGGEST_COMMAND("suggest_command"),
        CHANGE_PAGE("change_page"),
        COPY_TO_CLIPBOARD("copy_to_clipboard");

        private final String id;

        ClickAction(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static ClickAction getById(String id) {
            for(ClickAction t : values()) {
                if(t.id.equals(id)) return t;
            }
            return null;
        }
    }

}
