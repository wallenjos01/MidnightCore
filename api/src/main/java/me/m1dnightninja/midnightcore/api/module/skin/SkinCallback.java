package me.m1dnightninja.midnightcore.api.module.skin;

import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.util.UUID;


public interface SkinCallback {

    void onSkinAvailable(UUID pl, Skin skin);
}

