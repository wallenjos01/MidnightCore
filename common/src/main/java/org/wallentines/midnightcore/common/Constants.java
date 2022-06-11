package org.wallentines.midnightcore.common;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.requirement.CooldownRequirementType;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightcore.common.module.data.DataModuleImpl;
import org.wallentines.midnightcore.common.module.lang.LangModuleImpl;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.math.Region;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.requirement.Requirement;

public final class Constants {

    public static final Version VERSION = Version.SERIALIZER.deserialize("1.0.0");
    public static final String DEFAULT_NAMESPACE = "midnightcore";

    public static final ConfigSection CONFIG_DEFAULTS = new ConfigSection()
            .with("modules", new ConfigSection());

    public static String makeNode(String node) {
        return DEFAULT_NAMESPACE + "." + node;
    }

    @SuppressWarnings("unchecked")
    public static void registerDefaults() {

        ConfigRegistry.INSTANCE.setupDefaults("minecraft");

        ConfigRegistry.INSTANCE.registerSerializer(MStyle.class, MStyle.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MClickEvent.class, MClickEvent.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MHoverEvent.class, MHoverEvent.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MComponent.class, MComponent.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(MItemStack.class, AbstractItem.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(Skin.class, Skin.SERIALIZER);

        ConfigRegistry.INSTANCE.registerSerializer((Class<Requirement<MPlayer>>) ((Class<?>)Requirement.class), new Requirement.RequirementSerializer<>(Registries.REQUIREMENT_REGISTRY));

        ConfigRegistry.INSTANCE.registerInlineSerializer(MComponent.class, MComponent.INLINE_SERIALIZER);
        ConfigRegistry.INSTANCE.registerInlineSerializer(Location.class, Location.SERIALIZER);
        ConfigRegistry.INSTANCE.registerInlineSerializer(TextColor.class, TextColor.SERIALIZER);

        Registries.MODULE_REGISTRY.register(DataModuleImpl.ID, DataModuleImpl.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(LangModuleImpl.ID, LangModuleImpl.MODULE_INFO);


        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "cooldown"), new CooldownRequirementType());
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "permission"), (pl,req,data) -> pl.hasPermission(data));
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "world"), (pl,req,data) -> pl.getLocation().getWorldId().equals(Identifier.parse(data)));
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "in_region"), (pl,req,data) -> {

            Region reg = Region.SERIALIZER.deserialize(data);
            return reg.isWithin(pl.getLocation().getCoordinates());
        });

        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "locale"), (pl,req,data) -> data.equals(pl.getLocale()));

    }

}
