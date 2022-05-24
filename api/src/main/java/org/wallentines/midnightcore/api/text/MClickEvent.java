package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;

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

        return SERIALIZER.serialize(this).toString();
    }

    public static final ConfigSerializer<MClickEvent> SERIALIZER = ConfigSerializer.create(
            ConfigSerializer.entry(String.class, "action", ev -> ev.action.getId()),
            ConfigSerializer.entry(String.class, "value", ev -> ev.value),
            (action, value) -> new MClickEvent(ClickAction.getById(action), value));

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
