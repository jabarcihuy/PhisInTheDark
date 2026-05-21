package com.phisinthedark.audio;

import com.phisinthedark.assets.AssetLoader;

import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class AudioManager {
    private boolean muted;
    private boolean ambienceMuted;
    private Clip ambienceClip;

    public void playTyping() {
        playSound("typing.wav");
    }

    public void playNotification() {
        playSound("notification.wav");
    }

    public void playStatic() {
        playSound("static_noise.wav");
    }

    public void playGlitch() {
        playSound("glitch.wav");
    }

    public void playError() {
        playSound("error.wav");
    }

    public void playButtonClick() {
        playSound("button_click.wav");
    }

    public void playWhisper() {
        playSound("creepy_whisper.wav");
    }

    public void playAmbienceTick() {
        playSound("ambience_loop.wav");
    }

    public void loopAmbience() {
        if (muted || ambienceMuted || ambienceClip != null) {
            return;
        }
        ambienceClip = openClip("ambience_loop.wav");
        if (ambienceClip != null) {
            ambienceClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopAmbience() {
        if (ambienceClip != null) {
            ambienceClip.stop();
            ambienceClip.close();
            ambienceClip = null;
        }
    }

    private void softBeep() {
        if (!muted) {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (RuntimeException | Error ignored) {
                // Audio is optional; missing desktop sound support should not break gameplay.
            }
        }
    }

    private void playSound(String fileName) {
        if (muted) {
            return;
        }

        Clip clip = openClip(fileName);
        if (clip == null) {
            softBeep();
            return;
        }

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
            }
        });
        clip.start();
    }

    private Clip openClip(String fileName) {
        Path path = AssetLoader.soundPath(fileName);
        if (!Files.exists(path)) {
            return null;
        }

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(path.toFile())) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (Exception exception) {
            return null;
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopAmbience();
        }
    }

    public boolean isAmbienceMuted() {
        return ambienceMuted;
    }

    public void setAmbienceMuted(boolean ambienceMuted) {
        this.ambienceMuted = ambienceMuted;
        if (ambienceMuted) {
            stopAmbience();
        }
    }
}
