package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public class PlayerChangeSettingsEvent extends Event {

    private final ServerPlayer player;
    private final String locale;
    private final int viewDistance;
    private final ChatVisiblity chatVisibility;
    private final boolean chatColors;
    private final int modelCustomisation;
    private final HumanoidArm mainHand;

    public PlayerChangeSettingsEvent(ServerPlayer player, String locale, int viewDistance, ChatVisiblity chatVisibility, boolean chatColors, int modelCustomisation, HumanoidArm mainHand) {
        this.player = player;
        this.locale = locale;
        this.viewDistance = viewDistance;
        this.chatVisibility = chatVisibility;
        this.chatColors = chatColors;
        this.modelCustomisation = modelCustomisation;
        this.mainHand = mainHand;
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
}
