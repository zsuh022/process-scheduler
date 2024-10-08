package scheduler.visualiser;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static scheduler.constants.Constants.MUSIC_PATH;

public class MusicPlayer {
    private static MusicPlayer musicPlayerInstance = null;
    private static MediaPlayer musicPlayer = null;

    private MusicPlayer() {
        loadMusic("jonkler.mp3");
    }

    public static void loadMusic(String filename) {
        File file = new File(MUSIC_PATH.concat(filename));

        musicPlayer = new MediaPlayer(new Media(file.toURI().toString()));
    }

    public static void play() {
        musicPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                musicPlayer.seek(Duration.ZERO);
            }
        });

        musicPlayer.play();
    }

    public static void stop() {
        musicPlayer.stop();
    }

    public static synchronized MusicPlayer getInstance() {
        if (musicPlayerInstance == null) {
            musicPlayerInstance = new MusicPlayer();
        }

        return musicPlayerInstance;
    }
}
