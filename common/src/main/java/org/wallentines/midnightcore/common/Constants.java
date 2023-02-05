package org.wallentines.midnightcore.common;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.requirement.CooldownRequirementType;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.api.module.session.AbstractSession;
import org.wallentines.midnightcore.common.util.Util;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.math.Region;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.StringRegistry;

public final class Constants {

    public static final Version VERSION = Version.SERIALIZER.deserialize("1.0.0");
    public static final String DEFAULT_NAMESPACE = MidnightCoreAPI.DEFAULT_NAMESPACE;

    public static final IllegalStateException MODULE_DISABLED = new IllegalStateException("Attempt to access disabled Module!");
    public static final ConfigSection CONFIG_DEFAULTS = new ConfigSection()
            .with("modules", new ConfigSection())
            .with("locale", "en_us");

    public static String makeNode(String node) {
        return DEFAULT_NAMESPACE + "." + node;
    }

    public static void registerDefaults() {

        // Requirements
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "cooldown"), new CooldownRequirementType());
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "permission"), (pl,req,data) -> pl.hasPermission(data));
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "world"), (pl,req,data) -> pl.getLocation().getWorldId().equals(Identifier.parse(data)));
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "in_region"), (pl,req,data) -> Region.parse(data).isWithin(pl.getLocation().getCoordinates()));
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "locale"), (pl,req,data) -> data.contains("_") ? pl.getLocale().equals(data) : pl.getLocale().startsWith(data));

        // Placeholders
        StringRegistry<PlaceholderSupplier<String>> inline = PlaceholderManager.INSTANCE.getInlinePlaceholders();
        StringRegistry<PlaceholderSupplier<MComponent>> placeholders = PlaceholderManager.INSTANCE.getPlaceholders();

        inline.register("player_username", PlaceholderSupplier.create(MPlayer.class, MPlayer::getUsername));
        inline.register("player_uuid", PlaceholderSupplier.create(MPlayer.class, mp -> mp.getUUID().toString()));
        inline.register("player_world", PlaceholderSupplier.create(MPlayer.class, mp -> mp.getLocation().getWorldId().toString()));
        inline.register("player_locale", PlaceholderSupplier.create(MPlayer.class, MPlayer::getLocale));
        inline.register("player_health", PlaceholderSupplier.create(MPlayer.class, mp -> ((int) mp.getHealth()) + ""));
        placeholders.register("player_name", PlaceholderSupplier.create(MPlayer.class, MPlayer::getName));

        inline.register("server_version", PlaceholderSupplier.create(Util.getOr(MidnightCoreAPI.getInstance(), inst -> inst.getGameVersion().toString(), () -> "Unknown")));
        inline.register(DEFAULT_NAMESPACE + "_version", PlaceholderSupplier.create(VERSION.toString()));

        LangProvider.registerPlaceholders(PlaceholderManager.INSTANCE);
        AbstractSession.registerPlaceholders(PlaceholderManager.INSTANCE);

    }

}
