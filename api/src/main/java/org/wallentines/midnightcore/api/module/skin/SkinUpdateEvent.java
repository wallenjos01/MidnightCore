package org.wallentines.midnightcore.api.module.skin;

import org.wallentines.midnightlib.event.Event;

public class SkinUpdateEvent extends Event {

    private final Skinnable skinnable;
    private final Skin oldSkin;
    private Skin newSkin;

    private boolean cancelled = false;

    public SkinUpdateEvent(Skinnable skinnable, Skin oldSkin, Skin newSkin) {
        this.skinnable = skinnable;
        this.oldSkin = oldSkin;
        this.newSkin = newSkin;
    }

    public Skinnable getSkinnable() {
        return skinnable;
    }

    public Skin getOldSkin() {
        return oldSkin;
    }

    public Skin getNewSkin() {
        return newSkin;
    }

    public void setNewSkin(Skin newSkin) {
        this.newSkin = newSkin;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
