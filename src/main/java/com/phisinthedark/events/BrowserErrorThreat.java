package com.phisinthedark.events;

import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.player.Player;
import com.phisinthedark.ui.DesktopUI;

public class BrowserErrorThreat extends AbstractThreat {
    public BrowserErrorThreat() {
        super("browser_error");
    }

    @Override
    public void execute(DesktopUI desktopUI, Player player, AudioManager audioManager) {
        player.increaseParanoia(1);
        audioManager.playGlitch();
        desktopUI.triggerBrowserGlitch("render warning: page remembers old input");
    }
}
