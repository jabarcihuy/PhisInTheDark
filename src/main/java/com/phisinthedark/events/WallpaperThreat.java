package com.phisinthedark.events;

import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.player.Player;
import com.phisinthedark.ui.DesktopUI;

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
