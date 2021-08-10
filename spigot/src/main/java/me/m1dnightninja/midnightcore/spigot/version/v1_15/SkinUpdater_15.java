package me.m1dnightninja.midnightcore.spigot.version.v1_15;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.module.skin.ISkinUpdater;
import me.m1dnightninja.midnightcore.spigot.util.NMSUtil;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SkinUpdater_15 implements ISkinUpdater {

    private boolean initialized;

    private Class<?> craftPlayer;
    private Class<?> craftItemStack;
    private Class<?> packetPlayOutPlayerInfo;

    private Class<?> entityPlayer;
    private Class<?> enumPlayerInfoAction;

    private Class<?> enumItemSlot;

    private Class<?> enumGameMode;

    //private Class<?> biomeManager;

    private Method getProfile;
    private Method getHandle;
    private Method getDataWatcher;
    private Method asNMSCopy;
    private Method getId;
    private Method sendPacket;
    private Method getWorldProvider;
    private Method getDimensionManager;
    //private Method hashSeed;
    private Method getGameMode;
    //private Method getPreviousGameMode;
    //private Method isDebugWorld;
    //private Method isFlatWorld;
    private Method triggerHealthUpdate;
    private Method getPlayerListName;
    private Method getWorldServer;
    private Method getWorldType;

    private Field playerConnection;
    private Field playerInteractManager;
    private Field abilities;
    private Field ping;

    private Constructor<?> playerInfoConstructor;
    private Constructor<?> playerInfoDataConstructor;
    private Constructor<?> entityEquipmentConstructor;
    private Constructor<?> entityDestroyConstructor;
    private Constructor<?> namedEntitySpawnConstructor;
    private Constructor<?> entityHeadRotationConstructor;
    private Constructor<?> entityMetadataConstructor;
    private Constructor<?> respawnConstructor;
    private Constructor<?> positionConstructor;
    private Constructor<?> abilitiesConstructor;

    @Override
    public boolean initialize() {

        try {
            // Classes

            craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
            craftItemStack = ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack");

            entityPlayer = ReflectionUtil.getNMSClass("EntityPlayer");
            Class<?> entityHuman;
            entityHuman = ReflectionUtil.getNMSClass("EntityHuman");
            Class<?> entity;
            entity = ReflectionUtil.getNMSClass("Entity");

            Class<?> packet;
            packet = ReflectionUtil.getNMSClass("Packet");

            packetPlayOutPlayerInfo = ReflectionUtil.getNMSClass("PacketPlayOutPlayerInfo");
            enumPlayerInfoAction = ReflectionUtil.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            Class<?> playerInfoData;
            playerInfoData = ReflectionUtil.getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData");

            Class<?> dataWatcher;
            dataWatcher = ReflectionUtil.getNMSClass("DataWatcher");

            enumItemSlot = ReflectionUtil.getNMSClass("EnumItemSlot");
            Class<?> itemStack;
            itemStack = ReflectionUtil.getNMSClass("ItemStack");

            enumGameMode = ReflectionUtil.getNMSClass("EnumGamemode");
            Class<?> iChatBaseComponent;
            iChatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");

            Class<?> packetPlayOutEntityEquipment;
            packetPlayOutEntityEquipment = ReflectionUtil.getNMSClass("PacketPlayOutEntityEquipment");
            Class<?> packetPlayOutEntityDestroy;
            packetPlayOutEntityDestroy = ReflectionUtil.getNMSClass("PacketPlayOutEntityDestroy");
            Class<?> packetPlayOutNamedEntitySpawn;
            packetPlayOutNamedEntitySpawn = ReflectionUtil.getNMSClass("PacketPlayOutNamedEntitySpawn");
            Class<?> packetPlayOutHeadRotation;
            packetPlayOutHeadRotation = ReflectionUtil.getNMSClass("PacketPlayOutEntityHeadRotation");
            Class<?> packetPlayOutEntityMetadata;
            packetPlayOutEntityMetadata = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata");

            Class<?> worldServer;
            worldServer = ReflectionUtil.getNMSClass("WorldServer");
            Class<?> dimensionManager;
            dimensionManager = ReflectionUtil.getNMSClass("DimensionManager");
            Class<?> worldType;
            worldType = ReflectionUtil.getNMSClass("WorldType");

            Class<?> packetPlayOutRespawn;
            packetPlayOutRespawn = ReflectionUtil.getNMSClass("PacketPlayOutRespawn");

            Class<?> worldProvider = ReflectionUtil.getNMSClass("WorldProvider");

            Class<?> packetPlayOutPosition = ReflectionUtil.getNMSClass("PacketPlayOutPosition");

            Class<?> packetPlayOutAbilities;
            packetPlayOutAbilities = ReflectionUtil.getNMSClass("PacketPlayOutAbilities");

            Class<?> plConnection;
            plConnection = ReflectionUtil.getNMSClass("PlayerConnection");
            Class<?> world;
            world = ReflectionUtil.getNMSClass("World");
            //biomeManager = ReflectionUtil.getNMSClass("BiomeManager");
            Class<?> interactManager;
            interactManager = ReflectionUtil.getNMSClass("PlayerInteractManager");
            Class<?> playerAbilities;
            playerAbilities = ReflectionUtil.getNMSClass("PlayerAbilities");

            // Methods

            getProfile = ReflectionUtil.getMethodByReturnType(craftPlayer, GameProfile.class);
            getHandle = ReflectionUtil.getMethodByReturnType(craftPlayer, entityPlayer);
            getDataWatcher = ReflectionUtil.getMethodByReturnType(entity, dataWatcher);
            asNMSCopy = ReflectionUtil.getMethodByReturnType(craftItemStack, itemStack, ItemStack.class);
            getId = ReflectionUtil.getMethod(entity, "getId");
            sendPacket = ReflectionUtil.getMethod(plConnection, "sendPacket", packet);
            getWorldProvider = ReflectionUtil.getMethodByReturnType(world, worldProvider);
            getDimensionManager = ReflectionUtil.getMethodByReturnType(worldProvider, dimensionManager);
            //getDimensionKey = ReflectionUtil.getMethod(world, "getDimensionKey");
            //hashSeed = ReflectionUtil.getMethodByReturnType(biomeManager, long.class, long.class);
            getGameMode = ReflectionUtil.getMethod(interactManager, "getGameMode");
            //getPreviousGameMode = ReflectionUtil.getMethod(interactManager, "c");
            //isDebugWorld = ReflectionUtil.getMethod(world, "isDebugWorld");
            //isFlatWorld = ReflectionUtil.getMethod(worldServer, "isFlatWorld");
            triggerHealthUpdate = ReflectionUtil.getMethod(entityPlayer, "triggerHealthUpdate");
            getPlayerListName = ReflectionUtil.getMethod(entityPlayer, "getPlayerListName");
            getWorldServer = ReflectionUtil.getMethodByReturnType(entityPlayer, worldServer);
            getWorldType = ReflectionUtil.getMethodByReturnType(world, worldType);

            // Fields

            playerConnection = ReflectionUtil.getFieldByType(entityPlayer, plConnection);
            playerInteractManager = ReflectionUtil.getFieldByType(entityPlayer, interactManager);
            abilities = ReflectionUtil.getFieldByType(entityHuman, playerAbilities);
            ping = ReflectionUtil.getField(entityPlayer, "ping");

            playerInfoConstructor = ReflectionUtil.getConstructor(packetPlayOutPlayerInfo, enumPlayerInfoAction, ReflectionUtil.getArrayClass(entityPlayer));
            playerInfoDataConstructor = ReflectionUtil.getConstructor(playerInfoData, packetPlayOutPlayerInfo, GameProfile.class, int.class, enumGameMode, iChatBaseComponent);
            entityEquipmentConstructor = ReflectionUtil.getConstructor(packetPlayOutEntityEquipment, int.class, enumItemSlot, itemStack);
            entityDestroyConstructor = ReflectionUtil.getConstructor(packetPlayOutEntityDestroy, int[].class);
            namedEntitySpawnConstructor = ReflectionUtil.getConstructor(packetPlayOutNamedEntitySpawn, entityHuman);
            entityHeadRotationConstructor = ReflectionUtil.getConstructor(packetPlayOutHeadRotation, entity, byte.class);
            entityMetadataConstructor = ReflectionUtil.getConstructor(packetPlayOutEntityMetadata, int.class, dataWatcher, boolean.class);
            respawnConstructor = ReflectionUtil.getConstructor(packetPlayOutRespawn, dimensionManager, long.class, worldType, enumGameMode);
            positionConstructor = ReflectionUtil.getConstructor(packetPlayOutPosition, double.class, double.class, double.class, float.class, float.class, Set.class, int.class);
            abilitiesConstructor = ReflectionUtil.getConstructor(packetPlayOutAbilities, playerAbilities);
        } catch(IllegalStateException ex) {
            return false;
        }

        initialized = true;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updatePlayer(Player player, Skin skin, Collection<? extends Player> observers) {
        if(!initialized) return;

        Object ep = ReflectionUtil.callMethod(ReflectionUtil.castTo(player, craftPlayer), getHandle, false);
        GameProfile old = NMSUtil.getGameProfile(player);

        Object oid = ReflectionUtil.callMethod(ep, getId, false);
        if(!(oid instanceof Integer)) return;
        int id = (int) oid;


        GameProfile prof = new GameProfile(old.getId(), old.getName());

        prof.getProperties().putAll(old.getProperties());

        if(skin != null) {
            prof.getProperties().get("textures").clear();
            prof.getProperties().put("textures", new Property("textures", skin.getBase64(), skin.getSignature()));
        }

        Object eps = Array.newInstance(entityPlayer, 1);
        Array.set(eps, 0, ep);

        Object remove = ReflectionUtil.construct(playerInfoConstructor, ReflectionUtil.getEnumValue(enumPlayerInfoAction, "REMOVE_PLAYER"), eps);
        Object add = ReflectionUtil.construct(playerInfoConstructor, ReflectionUtil.getEnumValue(enumPlayerInfoAction, "ADD_PLAYER"), eps);

        try {
            Field entries = packetPlayOutPlayerInfo.getDeclaredField("b");
            entries.setAccessible(true);

            Object f = entries.get(add);
            if(f instanceof List) {

                List<Object> data = (List<Object>) f;

                Object infoData = ReflectionUtil.construct(playerInfoDataConstructor, add, prof, ReflectionUtil.getFieldValue(ep, ping, false), ReflectionUtil.getEnumValue(enumGameMode, player.getGameMode().name()), ReflectionUtil.callMethod(ep, getPlayerListName, false));

                data.set(0, infoData);
            }

        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        List<Object> items = new ArrayList<>();

        items.add(ReflectionUtil.construct(entityEquipmentConstructor, id, ReflectionUtil.getEnumValue(enumItemSlot, "MAINHAND"), ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, ReflectionUtil.castTo(player.getInventory().getItemInMainHand(), craftItemStack))));
        items.add(ReflectionUtil.construct(entityEquipmentConstructor, id, ReflectionUtil.getEnumValue(enumItemSlot, "OFFHAND"),  ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, ReflectionUtil.castTo(player.getInventory().getItemInOffHand(),  craftItemStack))));
        items.add(ReflectionUtil.construct(entityEquipmentConstructor, id, ReflectionUtil.getEnumValue(enumItemSlot, "HEAD"),     ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, ReflectionUtil.castTo(player.getInventory().getHelmet(),         craftItemStack))));
        items.add(ReflectionUtil.construct(entityEquipmentConstructor, id, ReflectionUtil.getEnumValue(enumItemSlot, "CHEST"),    ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, ReflectionUtil.castTo(player.getInventory().getChestplate(),     craftItemStack))));
        items.add(ReflectionUtil.construct(entityEquipmentConstructor, id, ReflectionUtil.getEnumValue(enumItemSlot, "LEGS"),     ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, ReflectionUtil.castTo(player.getInventory().getLeggings(),       craftItemStack))));
        items.add(ReflectionUtil.construct(entityEquipmentConstructor, id, ReflectionUtil.getEnumValue(enumItemSlot, "FEET"),     ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, ReflectionUtil.castTo(player.getInventory().getBoots(),          craftItemStack))));

        Object is = Array.newInstance(int.class, 1);
        Array.set(is, 0, id);

        Object destroy = ReflectionUtil.construct(entityDestroyConstructor, is);
        Object spawn = ReflectionUtil.construct(namedEntitySpawnConstructor, ep);
        Object tracker = ReflectionUtil.construct(entityMetadataConstructor, id, ReflectionUtil.callMethod(ep, getDataWatcher, false), true);

        float headRot = player.getEyeLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;

        Object head = ReflectionUtil.construct(entityHeadRotationConstructor, ep, (byte) ((rot * 256.0F) / 360.0F));

        for(Player p : observers) {

            Object op = ReflectionUtil.callMethod(ReflectionUtil.castTo(p, craftPlayer), getHandle, false);

            Object conn = ReflectionUtil.getFieldValue(op, playerConnection, false);
            ReflectionUtil.callMethod(conn, sendPacket, false, remove);
            ReflectionUtil.callMethod(conn, sendPacket, false, add);

            for(Object o : items) {
                ReflectionUtil.callMethod(conn, sendPacket, false, o);
            }


            if(p == player || !p.getWorld().equals(player.getWorld())) continue;

            ReflectionUtil.callMethod(conn, sendPacket, false, destroy);
            ReflectionUtil.callMethod(conn, sendPacket, false, spawn);
            ReflectionUtil.callMethod(conn, sendPacket, false, head);
            ReflectionUtil.callMethod(conn, sendPacket, false, tracker);
        }

        if (observers.contains(player)) {

            Object world = ReflectionUtil.callMethod(ep, getWorldServer, false);
            Object worldProv = ReflectionUtil.callMethod(world, getWorldProvider, false);

            Object interactionManager = ReflectionUtil.getFieldValue(ep, playerInteractManager, false);

            long seed = player.getWorld().getSeed();
            long hash = 0L;
            try {
                MessageDigest d = MessageDigest.getInstance("SHA-256");
                byte[] enc = d.digest(("" + seed).getBytes(StandardCharsets.UTF_8));

                for(int i = 0; i < 8; ++i) {
                    hash = (hash << 8) + (long)(enc[i] & 255);
                }
            } catch (NoSuchAlgorithmException var14) {
                var14.printStackTrace();
            }

            Object respawn = ReflectionUtil.construct(respawnConstructor,
                    ReflectionUtil.callMethod(worldProv, getDimensionManager, false),
                    hash,
                    ReflectionUtil.callMethod(world, getWorldType, false),
                    ReflectionUtil.callMethod(interactionManager, getGameMode, false)
            );

            Object position = ReflectionUtil.construct(positionConstructor, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch(), Sets.newHashSet(), 0);
            Object abil = ReflectionUtil.construct(abilitiesConstructor, ReflectionUtil.getFieldValue(ep, abilities, false));

            Object conn = ReflectionUtil.getFieldValue(ep, playerConnection, false);

            ReflectionUtil.callMethod(conn, sendPacket, false, respawn);
            ReflectionUtil.callMethod(conn, sendPacket, false, position);
            ReflectionUtil.callMethod(conn, sendPacket, false, abil);

            ReflectionUtil.callMethod(ep, triggerHealthUpdate, false);

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.updateCommands();
                    player.updateInventory();

                }
            }.runTask(MidnightCore.getPlugin(MidnightCore.class));

        }
    }
}
