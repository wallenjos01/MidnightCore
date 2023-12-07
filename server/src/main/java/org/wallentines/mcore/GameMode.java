package org.wallentines.mcore;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

public enum GameMode {

    SURVIVAL("survival"),
    CREATIVE("creative"),
    ADVENTURE("adventure"),
    SPECTATOR("spectator");

    private final String id;

    GameMode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static GameMode byId(String id) {
        for(GameMode mode : values()) {
            if(mode.id.equals(id)) return mode;
        }
        return null;
    }

    public static final Serializer<GameMode> SERIALIZER = InlineSerializer.of(GameMode::getId, GameMode::byId);

}
