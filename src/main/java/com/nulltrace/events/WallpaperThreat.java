package com.nulltrace.events;

import com.nulltrace.audio.AudioManager;
import com.nulltrace.player.Player;
import com.nulltrace.ui.DesktopUI;

public class WallpaperThreat extends AbstractThreat {
    public WallpaperThreat() {
        super("wallpaper_corrupt");
    }

    @Override
    public void execute(DesktopUI desktopUI, Player player, AudioManager audioManager) {
        player.increaseParanoia(1);
        audioManager.playStatic();
        desktopUI.corruptWallpaperTemporarily();
    }
}
