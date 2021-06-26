package me.m1dnightninja.midnightcore.spigot.version.v1_17;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class NMSUtil_17 implements NMSWrapper.NMSUtil {

    public static final UUID nullUid = new UUID(0L, 0L);

    private static final Class<?> craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
    private static final Method getHandle = ReflectionUtil.getMethod(craftPlayer, "getHandle");
    private static final Method getProfile = ReflectionUtil.getMethod(craftPlayer, "getProfile");

    private EntityPlayer toEntityPlayer(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (EntityPlayer) ReflectionUtil.callMethod(craftp, getHandle, false);
    }

    public GameProfile getGameProfile(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (GameProfile) ReflectionUtil.callMethod(craftp, getProfile, false);

    }

    public void sendMessage(Player player, MComponent comp) {

        EntityPlayer nmsPl = toEntityPlayer(player);

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(comp));
        nmsPl.sendMessage(message, nullUid);

    }

    @Override
    public void sendActionBar(Player pl, ActionBar ab) {

        EntityPlayer nmsPl = toEntityPlayer(pl);
        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(ab.getText()));

        nmsPl.a(message, true);
    }

    @Override
    public void sendTitle(Player pl, Title title) {

        EntityPlayer nmsPl = toEntityPlayer(pl);

        if(title.getOptions().clear) {
            nmsPl.b.sendPacket(new ClientboundClearTitlesPacket(true));
        }

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(title.getText()));

        if(title.getOptions().subtitle) {
            nmsPl.b.sendPacket(new ClientboundSetSubtitleTextPacket(message));
        } else {
            nmsPl.b.sendPacket(new ClientboundSetTitleTextPacket(message));
        }

        nmsPl.b.sendPacket(new ClientboundSetTitlesAnimationPacket(title.getOptions().fadeIn, title.getOptions().stay, title.getOptions().fadeOut));
    }

}
