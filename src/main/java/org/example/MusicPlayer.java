package org.example;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicPlayer {
    private Clip clip;

    public void playMusic(String filePath) {
        try {
            File musicPath = new File(filePath);

            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Зациклене відтворення
                clip.start();
            } else {
                System.out.println("Файл не знайдено: " + filePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
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
            File soundFile = new File(filePath);

            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                Clip effectClip = AudioSystem.getClip();
                effectClip.open(audioInput);
                effectClip.start();

                effectClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        effectClip.close();
                    }
                });
            } else {
                System.out.println("Файл звукового ефекту не знайдено: " + filePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}