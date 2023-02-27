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
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.*;

public class SkinUpdater_v1_19_R1_1190 implements SkinUpdater {

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void updateSkin(Player spl, Skin skin) {

        EntityPlayer epl = ((CraftPlayer) spl).getHandle();

        MinecraftServer server = epl.c;
        if(server == null) return;

        spl.closeInventory();

        // Create Packets
        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, epl);
        PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, epl);

        applyActiveProfile(add, spl, skin);

        List<Pair<EnumItemSlot, ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EnumItemSlot.a, epl.b(EnumHand.a)));
        items.add(new Pair<>(EnumItemSlot.b, epl.b(EnumHand.b)));
        items.add(new Pair<>(EnumItemSlot.c, epl.c(EnumItemSlot.c)));
        items.add(new Pair<>(EnumItemSlot.d, epl.c(EnumItemSlot.d)));
        items.add(new Pair<>(EnumItemSlot.e, epl.c(EnumItemSlot.e)));
        items.add(new Pair<>(EnumItemSlot.f, epl.c(EnumItemSlot.f)));

        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(epl.ae(), items);

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(epl.ae());
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);
        PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(epl.ae(), epl.ai(), true);

        float headRot = spl.getEyeLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0F) / 360.0F));

        WorldServer world = (WorldServer) epl.s;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.Z(),
                world.ab(),
                BiomeManager.a(world.B()),
                epl.d.b(),
                epl.d.c(),
                world.ae(),
                world.A(),
                true,
                Optional.empty()
        );

        PacketPlayOutPosition position = new PacketPlayOutPosition(spl.getLocation().getX(), spl.getLocation().getY(), spl.getLocation().getZ(), spl.getLocation().getYaw(), spl.getLocation().getPitch(), new HashSet<>(), 0, false);
        PacketPlayOutAbilities abilities = new PacketPlayOutAbilities(epl.fC());

        // Send Packets
        for(EntityPlayer obs : epl.c.ac().t()) {

            obs.b.a(remove);
            obs.b.a(add);

            if(obs == epl || !epl.s.equals(obs.s)) continue;

            obs.b.a(destroy);
            obs.b.a(spawn);
            obs.b.a(head);
            obs.b.a(tracker);
            obs.b.a(equip);

        }

        epl.b.a(respawn);
        epl.b.a(position);
        epl.b.a(abilities);
        epl.b.a(equip);

        server.g(() -> {
            server.ac().d(epl);
            server.ac().e(epl);

            spl.updateInventory();
        });
    }

    private void applyActiveProfile(PacketPlayOutPlayerInfo packet, Player player, Skin skin) {

        if(packet.c() != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a) return;
        List<PacketPlayOutPlayerInfo.PlayerInfoData> entries = packet.b();

        PacketPlayOutPlayerInfo.PlayerInfoData entry = null;

        int index = 0;
        for(; index < entries.size() ; index++) {
            PacketPlayOutPlayerInfo.PlayerInfoData ent = entries.get(index);
            for(MPlayer u : Objects.requireNonNull(MidnightCoreAPI.getRunningServer()).getPlayerManager()) {
                if(u.getUUID().equals(ent.a().getId())) {
                    entry = ent;
                    break;
                }
            }
        }
        if(entry == null) {
            return;
        }

        GameProfile profile = new GameProfile(entry.a().getId(), entry.a().getName());

        EntityPlayer epl = ((CraftPlayer) player).getHandle();

        profile.getProperties().clear();
        profile.getProperties().putAll(epl.fz().getProperties());

        if(skin != null) {
            profile.getProperties().get("textures").clear();
            profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }

        entries.set(index - 1, new PacketPlayOutPlayerInfo.PlayerInfoData(
                profile,
                entry.b(),
                entry.c(),
                entry.d(),
                entry.e()
        ));
    }
}
