package com.phisinthedark.events;

import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.player.Player;
import com.phisinthedark.ui.DesktopUI;

public interface Threat {
    String getName();

    void execute(DesktopUI desktopUI, Player player, AudioManager audioManager);
}
