package me.m1dnightninja.midnightcore.spigot.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderAPIIntegration {

    public static String getPlaceholderValue(String s, Player pl) {

        return PlaceholderAPI.setPlaceholders(pl, s);
    }

}
