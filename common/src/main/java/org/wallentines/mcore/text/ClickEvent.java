package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;


/**
 * A data type representing a chat component click event
 */
public class ClickEvent {

    private final Action action;
    private final String value;

    /**
     * Constructs a Click Event with the given {@link Action Action} type and value
     * @param action The type of click event
     * @param value The value given to the action
     */
    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    /**
     * Returns the Action type for this ClickEvent
     * @return The Action type
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the Action value for this ClickEvent
     * @return The Action value
     */
    public String getValue() {
        return value;
    }

    /**
     * A Serializer for creating ClickEvents from Mojang's chat component format
     */
    public static final Serializer<ClickEvent> SERIALIZER = ObjectSerializer.create(
            Action.SERIALIZER.entry("action", ClickEvent::getAction),
            Serializer.STRING.entry("value", ClickEvent::getValue),
            ClickEvent::new
    );


    /**
     * The type of action which will be performed when a ClickEvent is fired
     */
    public enum Action {

        /**
         * Opens a URL in the client's web browser when clicked
         */
        OPEN_URL("open_url"),

        /**
         * Opens a file on the client's computer when clicked.
         * Note: Clients will not accept this Action type from Servers.
         */
        OPEN_FILE("open_file"),

        /**
         * Runs a command on the client when clicked
         */
        RUN_COMMAND("run_command"),

        /**
         * Suggests a command for the client to run when clicked
         */
        SUGGEST_COMMAND("suggest_command"),

        /**
         * Sets the page in an open book when clicked. Only applies to text in books
         */
        CHANGE_PAGE("change_page"),

        /**
         * Copies text to the client's clipboard
         */
        COPY_TO_CLIPBOARD("copy_to_clipboard");

        /**
         * The id of the action type, for serialization
         */
        public final String id;

        Action(String id) {
            this.id = id;
        }

        /**
         * Returns the ID of the action type
         * @return The ID of the action type
         */
        public String getId() {
            return id;
        }

        /**
         * Finds the action type which has the given ID
         * @param id The ID to look up
         * @return The action type with the given ID, or null
         */
        public static Action byId(String id) {
            for(Action act : values()) {
                if(act.id.equals(id)) return act;
            }
            return null;
        }

        /**
         * A Serializer for converting an Action type to its ID, or looking one up by ID
         */
        public static final Serializer<Action> SERIALIZER = InlineSerializer.of(Action::getId, Action::byId);
    }

}
