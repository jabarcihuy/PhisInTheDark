package com.nulltrace.events;

import com.nulltrace.audio.AudioManager;
import com.nulltrace.player.Player;
import com.nulltrace.ui.DesktopUI;

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
