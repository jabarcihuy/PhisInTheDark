package com.phisinthedark.events;

import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.player.Player;
import com.phisinthedark.ui.DesktopUI;

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
