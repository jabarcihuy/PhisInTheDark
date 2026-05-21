package com.phisinthedark.events;

import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.player.Player;
import com.phisinthedark.ui.DesktopUI;

import javax.swing.Timer;
import java.util.List;
import java.util.Random;

public class EventSystem {
    private final DesktopUI desktopUI;
    private final Player player;
    private final AudioManager audioManager;
    private final List<Threat> threats;
    private final Random random;
    private final Timer timer;

    public EventSystem(DesktopUI desktopUI, Player player, AudioManager audioManager) {
        this.desktopUI = desktopUI;
        this.player = player;
        this.audioManager = audioManager;
        this.threats = List.of(
                new PopupThreat(),
                new BrowserErrorThreat(),
                new WallpaperThreat(),
                new HiddenMessageThreat()
        );
        this.random = new Random();
        this.timer = new Timer(16000, event -> triggerRandomEvent());
        this.timer.setInitialDelay(9000);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void triggerRandomEvent() {
        if (threats.isEmpty()) {
            return;
        }
        threats.get(random.nextInt(threats.size())).execute(desktopUI, player, audioManager);
    }

    public void triggerPuzzleSolvedEvent() {
        desktopUI.showPopup("ACCESS GRANTED", "system berubah. file baru muncul di desktop.");
        if (random.nextBoolean()) {
            desktopUI.corruptWallpaperTemporarily();
        }
    }
}
