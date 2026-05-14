package com.nulltrace.events;

import com.nulltrace.audio.AudioManager;
import com.nulltrace.player.Player;
import com.nulltrace.ui.DesktopUI;

public class HiddenMessageThreat extends AbstractThreat {
    public HiddenMessageThreat() {
        super("hidden_message");
    }

    @Override
    public void execute(DesktopUI desktopUI, Player player, AudioManager audioManager) {
        player.increaseParanoia(1);
        audioManager.playWhisper();
        desktopUI.showNotification("SYSTEM NOTE", "trace note: read the branch before typing.");
    }
}
