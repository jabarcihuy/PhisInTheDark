package com.nulltrace.events;

import com.nulltrace.audio.AudioManager;
import com.nulltrace.player.Player;
import com.nulltrace.ui.DesktopUI;

public class PopupThreat extends AbstractThreat {
    public PopupThreat() {
        super("unknown_popup");
    }

    @Override
    public void execute(DesktopUI desktopUI, Player player, AudioManager audioManager) {
        player.increaseParanoia(2);
        audioManager.playNotification();
        desktopUI.showPopup("UNKNOWN USER", "kamu masih membaca variable yang salah?");
    }
}
