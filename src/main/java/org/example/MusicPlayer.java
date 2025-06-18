package org.example;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MusicPlayer {
    private static MusicPlayer instance;
    private Clip clip;
    private boolean musicEnabled = true;
    private float volumePercent = 100;

    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled) stopMusic();
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void playMusic(String filePath) {
        if (!musicEnabled) return;

        try {
            stopMusic();

            URL url = getClass().getResource(filePath);
            if (url == null) {
                System.out.println("Файл не знайдено: " + filePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInput);
            setVolume(volumePercent);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void setVolume(float percent) {
        volumePercent = percent;

        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            if (percent == 0f) {
                gainControl.setValue(gainControl.getMinimum());
            } else {
                float gain = (float) (20f * Math.log10(percent / 100f));
                gain = Math.max(gain, gainControl.getMinimum());
                gainControl.setValue(gain);
            }
        }
    }


    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public void playEffect(String filePath) {
        try {
            URL url = getClass().getResource(filePath);
            if (url == null) {
                System.out.println("Файл звукового ефекту не знайдено: " + filePath);
                return;
            }
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip effectClip = AudioSystem.getClip();
            effectClip.open(audioInput);
            if (effectClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) effectClip.getControl(FloatControl.Type.MASTER_GAIN);
                float gain;
                if (volumePercent == 0f) {
                    gain = gainControl.getMinimum();
                } else {
                    gain = (float) (20f * Math.log10(volumePercent / 100f));
                    gain = Math.max(gain, gainControl.getMinimum());
                }
                gainControl.setValue(gain);
            }
            effectClip.start();

            effectClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    effectClip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playError() {
        playEffect("/assets/Sounds/error.wav");
    }
    public void playSuccess() {
        playEffect("/assets/Sounds/success.wav");
    }
    public void playFail() {
        playEffect("/assets/Sounds/fail.wav");
    }
    public void playSpin() {
        playEffect("/assets/Sounds/spin.wav");
    }
    public void playTick() {
        playEffect("/assets/Sounds/tick.wav");
    }



    public void playButtonClick() {
        playEffect("/assets/Sounds/select.wav");
    }
}