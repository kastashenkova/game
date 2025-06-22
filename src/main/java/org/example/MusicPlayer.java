package org.example;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Singleton class for managing background music and sound effects in the game.
 * Allows playing, stopping, and controlling the volume of music, as well as playing one-shot sound effects.
 */
public class MusicPlayer {
    private static MusicPlayer instance;
    private Clip clip; // Clip for background music
    private boolean musicEnabled = true; // Flag to control if music is enabled
    private float volumePercent = 100; // Current volume percentage

    /**
     * Returns the singleton instance of the MusicPlayer.
     * @return The single instance of MusicPlayer.
     */
    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    /**
     * Enables or disables music playback. If disabled, current music will stop.
     * @param enabled true to enable music, false to disable.
     */
    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled) stopMusic();
    }

    /**
     * Checks if music is currently enabled.
     * @return true if music is enabled, false otherwise.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Plays background music from the specified file path. If music is disabled, this method does nothing.
     * The music will loop continuously.
     * @param filePath The path to the audio file (e.g., WAV, AIFF).
     */
    public void playMusic(String filePath) {
        if (!musicEnabled) return;

        try {
            stopMusic(); // Stop any currently playing music

            URL url = getClass().getResource(filePath);
            if (url == null) {
                System.out.println("Файл із музикою не знайдено: " + filePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInput);
            setVolume(volumePercent); // Apply current volume setting
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop music indefinitely
            clip.start(); // Start playback

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the volume for the currently playing music.
     * @param percent The volume percentage (0-100). 0 means mute.
     */
    public void setVolume(float percent) {
        volumePercent = percent; // Store the desired volume percentage

        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            if (percent == 0f) {
                gainControl.setValue(gainControl.getMinimum()); // Set to minimum (mute)
            } else {
                // Convert percentage to decibels for the gain control
                float gain = (float) (20f * Math.log10(percent / 100f));
                // Ensure the calculated gain is within the supported range
                gain = Math.max(gain, gainControl.getMinimum());
                gainControl.setValue(gain);
            }
        }
    }


    /**
     * Stops and closes the currently playing background music.
     */
    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    /**
     * Plays a one-shot sound effect from the specified file path.
     * The volume of the effect is adjusted according to the global volume setting.
     * @param filePath The path to the audio file for the effect.
     */
    public void playEffect(String filePath) {
        try {
            URL url = getClass().getResource(filePath);
            if (url == null) {
                System.out.println("Sound effect file not found: " + filePath);
                return;
            }
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip effectClip = AudioSystem.getClip(); // Create a new Clip for the effect
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
            effectClip.start(); // Start playback of the effect

            // Add a listener to close the clip once the effect has finished playing
            effectClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    effectClip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays the error sound effect.
     */
    public void playError() {
        playEffect("/assets/Sounds/error.wav");
    }
    /**
     * Plays the success sound effect.
     */
    public void playSuccess() {
        playEffect("/assets/Sounds/success.wav");
    }
    /**
     * Plays the fail sound effect.
     */
    public void playFail() {
        playEffect("/assets/Sounds/fail.wav");
    }
    /**
     * Plays the spin sound effect.
     */
    public void playSpin() {
        playEffect("/assets/Sounds/spin.wav");
    }
    /**
     * Plays the tick sound effect.
     */
    public void playTick() {
        playEffect("/assets/Sounds/tick.wav");
    }


    /**
     * Plays the button click sound effect.
     */
    public void playButtonClick() {
        playEffect("/assets/Sounds/select.wav");
    }
}