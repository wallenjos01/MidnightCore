package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

/**
 * A data type representing a chat component HoverEvent
 */
public class HoverEvent {

    /**
     * The type of action to take when hovering
     */
    private final Action action;

    /**
     * The content of the action to take
     */
    private final ConfigSection contents;

    /**
     * Versions <1.16 used string values for Hover Events rather than Objects
     */
    private final String value;

    /**
     * Constructs a HoverEvent with the given Action and content
     * @param action The type of action to take when hovering
     * @param contents The action data
     */
    public HoverEvent(Action action, ConfigSection contents) {

        this(action, contents, null);
    }

    /**
     * Constructs a legacy HoverEvent with the given Action and string value
     * @param action The type of action to take when hovering
     * @param value The action data
     */
    public HoverEvent(Action action, String value) {

        this(action, null, value);
    }

    private HoverEvent(Action action, ConfigSection contents, String value) {

        if (contents != null && !GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.HOVER_CONTENTS)) {
            throw new IllegalArgumentException("Attempt to construct a hover event with contents on an unsupported version!");
        }

        if(action == Action.SHOW_ACHIEVEMENT && !GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.HOVER_SHOW_ACHIEVEMENT)) {
            throw new IllegalArgumentException("Attempt to create a show_achievement hover event on unsupported version!");
        }

        this.action = action;
        this.contents = contents;
        this.value = value;
    }

    /**
     * Returns the action type for this HoverEvent
     * @return The action type
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the content of this HoverEvent
     * @return The action content
     */
    public ConfigSection getContents() {
        return contents;
    }

    /**
     * Returns the legacy value of this HoverEvent
     * @return The legacy action value
     */
    public String getValue() {
        return value;
    }


    /**
     * Creates a new hover event to display some given text
     * @param hover The text to display
     * @return A new hover event
     */
    public static HoverEvent createTextHover(Component hover) {

        return new HoverEvent(Action.SHOW_TEXT, hover.toJSON());
    }

    /**
     * Creates a new hover event to display the attributes of a given ItemStack
     * @param stack The ItemStack to display
     * @return A new hover event
     */
    public static HoverEvent createItemHover(ItemStack stack) {

        return new HoverEvent(Action.SHOW_ITEM, new ConfigSection()
                .with("id", stack.getType().toString())
                .with("Count", stack.getCount())
                .with("tag", stack.getTag() == null ? null : JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, stack.getTag())));
    }

    /**
     * A Serializer for creating HoverEvents from Mojang's chat component format
     */
    public static final Serializer<HoverEvent> SERIALIZER = ObjectSerializer.create(
            Action.SERIALIZER.entry("action", HoverEvent::getAction),
            ConfigSection.SERIALIZER.entry("contents", HoverEvent::getContents).optional(),
            Serializer.STRING.entry("value", HoverEvent::getValue).optional(),
            HoverEvent::new
    );

    /**
     * The type of action which will be performed when a HoverEvent is fired
     */
    public enum Action {

        /**
         * Shows text in a tooltip when a component is hovered over
         */
        SHOW_TEXT("show_text"),

        /**
         * Shows an item tooltip when a component is hovered over
         */
        SHOW_ITEM("show_item"),

        /**
         * Shows entity data when a component is hovered over
         */
        SHOW_ENTITY("show_entity"),

        /**
         * Shows an achievement name and description when a component is hovered over.
         * Not used since pre-1.12
         */
        SHOW_ACHIEVEMENT("show_achievement");

        /**
         * The ID of the Action type
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
