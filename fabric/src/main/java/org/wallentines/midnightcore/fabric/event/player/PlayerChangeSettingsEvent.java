package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.wallentines.midnightlib.event.Event;

public class PlayerChangeSettingsEvent extends Event {

    private final ServerPlayer player;
    private final String locale;
    private final int viewDistance;
    private final ChatVisiblity chatVisibility;
    private final boolean chatColors;
    private final int modelCustomisation;
    private final HumanoidArm mainHand;
    private final boolean textFilteringEnabled;
    private final boolean allowsListing;

    public PlayerChangeSettingsEvent(ServerPlayer player, String locale, int viewDistance, ChatVisiblity chatVisibility, boolean chatColors, int modelCustomisation, HumanoidArm mainHand, boolean textFilteringEnabled, boolean allowsListing) {
        this.player = player;
        this.locale = locale;
        this.viewDistance = viewDistance;
        this.chatVisibility = chatVisibility;
        this.chatColors = chatColors;
        this.modelCustomisation = modelCustomisation;
        this.mainHand = mainHand;
        this.textFilteringEnabled = textFilteringEnabled;
        this.allowsListing = allowsListing;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getLocale() {
        return locale;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public ChatVisiblity getChatVisibility() {
        return chatVisibility;
    }

    public boolean isChatColors() {
        return chatColors;
    }

    public int getModelCustomisation() {
        return modelCustomisation;
    }

    public HumanoidArm getMainHand() {
        return mainHand;
    }

    public boolean isTextFilteringEnabled() {
        return textFilteringEnabled;
    }

    public boolean isAllowsListing() {
        return allowsListing;
    }
}
