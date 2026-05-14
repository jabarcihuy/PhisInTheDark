package com.nulltrace.events;

import com.nulltrace.audio.AudioManager;
import com.nulltrace.player.Player;
import com.nulltrace.ui.DesktopUI;

public interface Threat {
    String getName();

    void execute(DesktopUI desktopUI, Player player, AudioManager audioManager);
}
