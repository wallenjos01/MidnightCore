package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.BiomeManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class SkinUpdater_v1_17_R1 implements SkinUpdater {

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void updateSkin(Player spl, Skin skin) {

        EntityPlayer epl = ((CraftPlayer) spl).getHandle();

        MinecraftServer server = epl.c;
        if(server == null) return;

        // Create Packets

        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.c, epl);
        PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, epl);

        applyActiveProfile(add, skin);

        List<Pair<EnumItemSlot, ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EnumItemSlot.a, epl.b(EnumHand.a)));
        items.add(new Pair<>(EnumItemSlot.b, epl.b(EnumHand.b)));
        items.add(new Pair<>(EnumItemSlot.c, epl.getEquipment(EnumItemSlot.c)));
        items.add(new Pair<>(EnumItemSlot.d, epl.getEquipment(EnumItemSlot.d)));
        items.add(new Pair<>(EnumItemSlot.e, epl.getEquipment(EnumItemSlot.e)));
        items.add(new Pair<>(EnumItemSlot.f, epl.getEquipment(EnumItemSlot.f)));

        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(epl.getId(), items);

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(epl.getId());
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);
        PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(epl.getId(), epl.getDataWatcher(), true);

        float headRot = spl.getEyeLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0F) / 360.0F));

        WorldServer world = (WorldServer) epl.t;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.getDimensionManager(),
                world.getDimensionKey(),
                BiomeManager.a(world.getSeed()),
                epl.d.getGameMode(),
                epl.d.c(),
                world.isDebugWorld(),
                world.isFlatWorld(),
                true
        );

        PacketPlayOutPosition position = new PacketPlayOutPosition(spl.getLocation().getX(), spl.getLocation().getY(), spl.getLocation().getZ(), spl.getLocation().getYaw(), spl.getLocation().getPitch(), new HashSet<>(), 0, false);
        PacketPlayOutAbilities abilities = new PacketPlayOutAbilities(epl.getAbilities());

        // Send Packets
        for(EntityPlayer obs : epl.c.getPlayerList().getPlayers()) {

            obs.b.sendPacket(remove);
            obs.b.sendPacket(add);

            if(obs == epl || !epl.t.equals(obs.t)) continue;

            obs.b.sendPacket(destroy);
            obs.b.sendPacket(spawn);
            obs.b.sendPacket(head);
            obs.b.sendPacket(tracker);
            obs.b.sendPacket(equip);

        }

        epl.b.sendPacket(respawn);
        epl.b.sendPacket(position);
        epl.b.sendPacket(abilities);
        epl.b.sendPacket(equip);

        server.getPlayerList().d(epl);
        server.getPlayerList().updateClient(epl);

        spl.updateInventory();
    }

    private void applyActiveProfile(PacketPlayOutPlayerInfo packet, Skin skin) {

        if(packet.c() != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a) return;
        List<PacketPlayOutPlayerInfo.PlayerInfoData> entries = packet.b();

        GameProfile profile = null;
        for(PacketPlayOutPlayerInfo.PlayerInfoData ent : entries) {
            for(MPlayer u : Objects.requireNonNull(MidnightCoreAPI.getRunningServer()).getPlayerManager()) {
                if(u.getUUID().equals(ent.a().getId())) {
                    profile = ent.a();
                    break;
                }
            }
            if(profile != null) break;
        }

        if(profile == null) return;
        if(skin == null) return;

        GameProfile oldProfile = profile;

        profile = new GameProfile(oldProfile.getId(), oldProfile.getName());

        Player pl = Bukkit.getPlayer(profile.getId());
        if(pl == null) return;

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();

        profile.getProperties().clear();
        profile.getProperties().putAll(epl.getProfile().getProperties());

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

        entries.set(0, new PacketPlayOutPlayerInfo.PlayerInfoData(
                profile,
                pl.getPing(),
                epl.d.getGameMode(),
                epl.listName
        ));
    }
}
