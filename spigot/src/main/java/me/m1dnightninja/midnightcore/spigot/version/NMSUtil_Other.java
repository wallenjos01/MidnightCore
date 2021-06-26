package me.m1dnightninja.midnightcore.spigot.version;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NMSUtil_Other implements NMSWrapper.NMSUtil {

    public static final UUID nullUid = new UUID(0L, 0L);

    public GameProfile getGameProfile(Player player) {

        return new GameProfile(player.getUniqueId(), player.getName());
    }

    public void sendMessage(Player player, MComponent comp) {

        TextComponent cmp = new TextComponent(ComponentSerializer.parse(MComponent.Serializer.toJsonString(comp)));
        player.spigot().sendMessage(ChatMessageType.SYSTEM, nullUid, cmp);
    }

    @Override
    public void sendActionBar(Player pl, ActionBar ab) {

        TextComponent comp = new TextComponent(ComponentSerializer.parse(MComponent.Serializer.toJsonString(ab.getText())));
        pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, nullUid, comp);
    }

    @Override
    public void sendTitle(Player pl, Title title) {

        if(title.getOptions().clear) {
            pl.resetTitle();
        }

        String ttl = title.getText().toLegacyText(false);
        if(title.getOptions().subtitle) {
            pl.sendTitle(ttl, null, 20, 80, 20);
        } else {
            pl.sendTitle(null, ttl, 20, 80, 20);
        }

    }

}
