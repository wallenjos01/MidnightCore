package me.m1dnightninja.midnightcore.spigot.version.v1_17;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.module.skin.ISkinUpdater;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.level.biome.BiomeManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SkinUpdater_17 implements ISkinUpdater {

    private boolean initialized;

    private Class<?> craftPlayer;
    private Method getHandle;

    @Override
    public boolean initialize() {

        try {

            craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
            getHandle = ReflectionUtil.getMethodByReturnType(craftPlayer, EntityPlayer.class);

        } catch(IllegalStateException ex) {
            return false;
        }

        initialized = true;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updatePlayer(Player pl, Skin skin, Collection<? extends Player> observers) {
        if(!initialized) return;

        EntityPlayer player = (EntityPlayer) ReflectionUtil.callMethod(ReflectionUtil.castTo(pl, craftPlayer), getHandle, false);

        MinecraftServer server = player.getMinecraftServer();
        if(server == null) return;

        // Create Packets

        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, player);
        PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, player);

        GameProfile prof = new GameProfile(player.getProfile().getId(), player.getProfile().getName());

        prof.getProperties().putAll(player.getProfile().getProperties());

        if(skin != null) {
            prof.getProperties().get("textures").clear();
            prof.getProperties().put("textures", new Property("textures", skin.getBase64(), skin.getSignature()));
        }

        try {
            Field entries = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
            entries.setAccessible(true);

            Object f = entries.get(add);
            if(f instanceof List) {

                List<Object> data = (List<Object>) f;
                PacketPlayOutPlayerInfo.PlayerInfoData infoData = new PacketPlayOutPlayerInfo.PlayerInfoData(prof, pl.getPing(), player.d.getGameMode(), player.listName);

                data.set(0, infoData);
            }

        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EnumItemSlot.fromName("mainhand"), player.getItemInMainHand()));
        items.add(new Pair<>(EnumItemSlot.fromName("offhand"),  player.getItemInOffHand()));
        items.add(new Pair<>(EnumItemSlot.fromName("feet"),     player.getInventory().getArmorContents().get(3)));
        items.add(new Pair<>(EnumItemSlot.fromName("legs"),     player.getInventory().getArmorContents().get(2)));
        items.add(new Pair<>(EnumItemSlot.fromName("chest"),    player.getInventory().getArmorContents().get(1)));
        items.add(new Pair<>(EnumItemSlot.fromName("head"),     player.getInventory().getArmorContents().get(0)));

        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(player.getId(), items);


        // Hack to fix 1.17.0
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.writeInt(player.getId());

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(serializer);

        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(player);
        PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true);

        float headRot = pl.getLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(player, (byte) ((rot * 256.0F) / 360.0F));

        WorldServer world = player.getWorldServer();

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.getDimensionManager(),
                world.getDimensionKey(),
                BiomeManager.a(world.getSeed()),
                player.d.getGameMode(),
                player.d.c(),
                world.isDebugWorld(),
                world.isFlatWorld(),
                true
        );

        PacketPlayOutPosition position = new PacketPlayOutPosition(pl.getLocation().getX(), pl.getLocation().getY(), pl.getLocation().getZ(), pl.getLocation().getYaw(), pl.getLocation().getPitch(), new HashSet<>(), 0, false);
        PacketPlayOutAbilities abilities = new PacketPlayOutAbilities(player.getAbilities());

        // Send Packets

        for(EntityPlayer obs : server.getPlayerList().getPlayers()) {

            obs.b.sendPacket(remove);
            obs.b.sendPacket(add);

            obs.b.sendPacket(equip);

            if(obs == player || !player.getWorldServer().equals(obs.getWorldServer())) continue;

            obs.b.sendPacket(destroy);
            obs.b.sendPacket(spawn);
            obs.b.sendPacket(head);
            obs.b.sendPacket(tracker);

        }

        player.b.sendPacket(respawn);
        player.b.sendPacket(position);
        player.b.sendPacket(abilities);

        player.triggerHealthUpdate();

        new BukkitRunnable() {
            @Override
            public void run() {
                pl.updateCommands();
                pl.updateInventory();
            }
        }.runTask(MidnightCore.getPlugin(MidnightCore.class));

        player.getInventory().j();

    }
}
