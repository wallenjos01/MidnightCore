package org.wallentines.mcore.text;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A data type representing actual content within a Component which will be rendered as text to the client
 */
public abstract class Content {

    // The Content type (i.e. text, translate, keybind, etc.)
    protected final String type;

    protected Content(String type) {
        this.type = type;
    }

    /**
     * Returns the Content type
     * @return The Content type
     */
    public String getType() {
        return type;
    }

    /**
     * Whether this content needs to be resolved before being sent by the server
     * @return Whether the content needs resolution
     */
    public boolean requiresResolution() {
        return false;
    }

    /**
     * A Content type which displays plaintext
     */
    public static class Text extends Content {

        /**
         * The text to display
         */
        public final String text;

        public Text(String text) {
            super("text");
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Text text1 = (Text) o;
            return Objects.equals(text, text1.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        public static final Serializer<Text> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("text", con -> con.text),
                Text::new
        );

        public static final InlineSerializer<Text> PLAIN_SERIALIZER = InlineSerializer.of(text -> text.text, Text::new);
    }


    /**
     * A Content type which displays Translatable text
     */
    public static class Translate extends Content {

        /**
         * The key of the text to translate on the client
         */
        public final String key;

        /**
         * The fallback text to use if the key is not found in the user's language
         */
        public final String fallback;

        /**
         * A collection of components to insert into placeholders in the translatable text
         */
        public final Collection<Component> with;

        public Translate(String key) {
            this(key, null, null);
        }
        public Translate(String key, String fallback, Collection<Component> with) {
            super("translate");
            this.key = key;
            this.fallback = fallback;
            this.with = with == null ? List.of() : List.copyOf(with);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Translate translate = (Translate) o;
            return Objects.equals(key, translate.key) && Objects.equals(fallback, translate.fallback) && Objects.equals(with, translate.with);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, fallback, with);
        }

        public static Serializer<Translate> serializer(Serializer<Component> serializer) {

            return ObjectSerializer.create(
                    Serializer.STRING.entry("translate", con -> con.key),
                    Serializer.STRING.<Translate>entry("fallback", con -> con.fallback).optional(),
                    serializer.listOf().<Translate>entry("with", con -> con.with).optional(),
                    Translate::new
            );
        }

        public static final InlineSerializer<Translate> PLAIN_SERIALIZER = InlineSerializer.of(
                tra -> tra.fallback == null ? tra.key : tra.fallback,
                txt -> { throw new IllegalStateException("Unable to deserialize a Translate component from plaintext!"); });
    }


    /**
     * A Content type which displays the key bound to a specific action on the client
     */
    public static class Keybind extends Content {

        /**
         * The ID of the keybind to display
         */
        public final String key;

        public Keybind(String key) {
            super("keybind");
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Keybind keybind = (Keybind) o;
            return Objects.equals(key, keybind.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        public static final Serializer<Keybind> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("keybind", con -> con.key),
                Keybind::new
        );

        public static final InlineSerializer<Keybind> PLAIN_SERIALIZER = InlineSerializer.of(
                bind -> bind.key,
                txt -> { throw new IllegalStateException("Unable to deserialize a Keybind component from plaintext!"); });
    }


    /**
     * A Content type which displays the scoreboard score of a specific entity and objective
     */
    public static class Score extends Content {

        /**
         * The username of the Player or UUID of the entity whose score to look up
         */
        public final String name;

        /**
         * The objective to look up
         */
        public final String objective;

        /**
         * If present, this will display regardless of what the actual score is.
         * TODO: Find out if this was discontinued, as it seems unimplemented in the current version
         */
        public final String value;

        public Score(String name) {
            this(name, null, null);
        }

        public Score(String name, String objective, String value) {
            super("score");
            this.name = name;
            this.objective = objective;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Score score = (Score) o;
            return Objects.equals(name, score.name) && Objects.equals(objective, score.objective) && Objects.equals(value, score.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, objective, value);
        }

        public static final Serializer<Score> SERIALIZER = ObjectSerializer.create(
                ConfigSection.SERIALIZER.entry("score", con -> new ConfigSection()
                        .with("name", con.name)
                        .with("objective", con.objective)
                        .with("value", con.value)),
                (cfg) -> new Score(
                        cfg.getString("name"),
                        cfg.getOrDefault("objective", (String) null),
                        cfg.getOrDefault("value", (String) null)
                )
        );

        public static final InlineSerializer<Score> PLAIN_SERIALIZER = InlineSerializer.of(
                score -> "",
                txt -> { throw new IllegalStateException("Unable to deserialize a Score component from plaintext!"); });
    }


    /**
     * A content type which display's the display names of any entities which apply to the given selector text.
     */
    public static class Selector extends Content {

        /**
         * The selector text to lookup
         */
        public final String value;

        /**
         * The component to use to separate entries
         */
        public final Component separator;

        public Selector(String value) {
            this(value, null);
        }

        public Selector(String value, Component separator) {
            super("selector");
            this.value = value;
            this.separator = separator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Selector selector = (Selector) o;
            return Objects.equals(value, selector.value) && Objects.equals(separator, selector.separator);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, separator);
        }

        public static final Serializer<Selector> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("selector", con -> con.value),
                Selector::new
        );

        public static final InlineSerializer<Selector> PLAIN_SERIALIZER = InlineSerializer.of(
                sel -> "",
                txt -> { throw new IllegalStateException("Unable to deserialize a Selector component from plaintext!"); });
    }


    /**
     * A Content type which looks up NBT data on entities, block entities, or command storage
     */
    public static class NBT extends Content {

        /**
         * The NBT path to look up
         */
        public final String path;

        /**
         * Whether the value at the NBT path should be interpreted as a text Component
         */
        public final boolean interpret;

        /**
         * The Component to use to separate entries
         */
        public final Component separator;

        /**
         * The type of object to look up NBT data on
         */
        public final DataSourceType type;

        /**
         * Some data used to determine which object of the given type will be used for lookup
         * This is coordinates for blocks, UUIDs for entities, and resource locations for command storage entries.
         */
        public final String data;

        public NBT(String path, Boolean interpret, Component separator, DataSourceType type, String dataSource) {
            super("nbt");
            this.path = path;
            this.interpret = interpret != null && interpret;
            this.separator = separator;
            this.type = type;
            this.data = dataSource;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NBT nbt = (NBT) o;
            return interpret == nbt.interpret &&
                    Objects.equals(path, nbt.path) &&
                    Objects.equals(separator, nbt.separator) &&
                    type == nbt.type && Objects.equals(data, nbt.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, interpret, separator, type, data);
        }

        public enum DataSourceType {
            BLOCK,
            ENTITY,
            STORAGE
        }

        public static Serializer<NBT> serializer(Serializer<Component> serializer) {

            return ObjectSerializer.create(
                    Serializer.STRING.entry("nbt", con -> con.path),
                    Serializer.BOOLEAN.<NBT>entry("interpret", con -> con.interpret).optional(),
                    serializer.<NBT>entry("separator", con -> con.separator).optional(),
                    Serializer.STRING.<NBT>entry("block", con -> con.type == DataSourceType.BLOCK ? con.data : null).optional(),
                    Serializer.STRING.<NBT>entry("entity", con -> con.type == DataSourceType.ENTITY ? con.data : null).optional(),
                    Serializer.STRING.<NBT>entry("storage", con -> con.type == DataSourceType.STORAGE ? con.data : null).optional(),
                    (path, interpret, sep, block, entity, storage) -> {

                        String data;
                        DataSourceType type;
                        if(block != null) {
                            data = block;
                            type = DataSourceType.BLOCK;
                        }
                        else if(entity != null) {
                            data = entity;
                            type = DataSourceType.ENTITY;
                        }
                        else if(storage != null) {
                            data = storage;
                            type = DataSourceType.STORAGE;
                        } else {
                            throw new IllegalArgumentException("Not enough data to deserialize NBT component!");
                        }

                        return new NBT(path, interpret, sep, type, data);
                    }
            );
        }

        public static final InlineSerializer<NBT> PLAIN_SERIALIZER = InlineSerializer.of(
                nbt -> "",
                txt -> { throw new IllegalStateException("Unable to deserialize a NBT component from plaintext!"); });
    }

}
