package ru.apache_maven;

import javafx.scene.media.AudioClip;

public class SoundManager {
    // Путь к звуковым файлам должен быть корректным и файлы должны находиться в resources (например, resources/sounds/)
    private static final AudioClip moveSound = new AudioClip(SoundManager.class.getResource("/sounds/move-self.wav").toExternalForm());
    private static final AudioClip checkSound = new AudioClip(SoundManager.class.getResource("/sounds/move-check.wav").toExternalForm());
    private static final AudioClip captureSound = new AudioClip(SoundManager.class.getResource("/sounds/capture.wav").toExternalForm());
    private static final AudioClip checkmateSound = new AudioClip(SoundManager.class.getResource("/sounds/notify.wav").toExternalForm());

    public static void playMoveSound() {
        moveSound.play();
    }

    public static void playCheckSound() {
        checkSound.play();
    }

    public static void playCaptureSound() {
        captureSound.play();
    }

    public static void playCheckmateSound() {
        checkmateSound.play();
    }
}
