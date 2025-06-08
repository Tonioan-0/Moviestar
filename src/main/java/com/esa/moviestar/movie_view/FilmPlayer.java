package com.esa.moviestar.movie_view;

import com.esa.moviestar.Main;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;

import java.io.IOException;
import java.net.URL;

//To facilitate understanding, I've organized this class in a manner that allows information to be collapsed, making it more easily understandable ⁓Antonio D'Ambrosio
// Sources:
// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/media/MediaView.html
// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/media/MediaPlayer.html
public class FilmPlayer{
    //Buttons (They are stackPane to handle the icon better, with button I would use .setGraphics()
    public StackPane playerContainer;
    public StackPane btnPlay;
    public StackPane btnReturn;
    public StackPane btnForward;
    public StackPane btnMaximize;
    public StackPane btnSpeed;
    public StackPane closeButton;


    public Text textActualTime;
    public Text textTotalTime;

    //I don't know why, but the sliders don't have any status of progress (I can't modify the color of the region that before the thumb
    //and even if I found a way using https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html), it will compromise the user experience (lag)
    //So I created this structure Container -|-> ProgressBar
    //                                       |-> Slider
    // Then I bind them
    public StackPane volumeIconContainer;
    public AnchorPane volumeSliderContainer;
    public Slider sliderVolume;
    public Slider sliderVideo;
    public ProgressBar pbrVolume;
    public ProgressBar pbrVideo;

    public SVGPath iconSpeed;
    public SVGPath iconMaximize;
    public SVGPath iconPlay;
    public SVGPath iconVolume;


    public AnchorPane root;
    public VBox bottomBar;

    protected MediaPlayer mediaPlayer;
    protected MediaView mediaView;
    protected String videoUrl;

    //Variables
    private final double[] playbackSpeeds ={0.5 , 0.7, 1.0, 1.2, 1.5, 2.0};
    private int currentSpeedIndex = 2;
    private boolean isMuted = false;
    private double previousVolume = 1.0;
    private boolean isMaximized = false;
    private static final double SKIP_DURATION = 10.0;
    private boolean controlsVisible = true;
    private static final double HIDE_DELAY_SECONDS = 3.0;
    private static final double FADE_DURATION_MILLIS = 300.0;

    //Transition
    private Timeline hideControlsTimer;
    private FadeTransition fadeOutTransition; // For bottomBar
    private FadeTransition fadeInTransition;  // For bottomBar
    private FadeTransition fadeOutTransitionCloseButton;
    private FadeTransition fadeInTransitionCloseButton;

    //Data to return to home
    private User user;
    private Account account;

    public void initialize(){
        setupControlActions();
        setupAutoHideControls();
        sliderVolume.setValue(100);
        sliderVideo.setValue(0);
        pbrVolume.progressProperty().bind(sliderVolume.valueProperty().divide(100));
        pbrVideo.progressProperty().bind(sliderVideo.valueProperty().divide(100));
        closeButton.setOnMouseClicked(e-> returnToMainPages());
    }

    private void setupAutoHideControls(){
        // Transitions for bottomBar (manages controlsVisible state)
        fadeOutTransition = new FadeTransition(Duration.millis(FADE_DURATION_MILLIS), bottomBar);
        fadeOutTransition.setFromValue(1.0);
        fadeOutTransition.setToValue(0.0);
        fadeOutTransition.setOnFinished(e -> controlsVisible = false);

        fadeInTransition = new FadeTransition(Duration.millis(FADE_DURATION_MILLIS), bottomBar);
        fadeInTransition.setFromValue(0.0);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setOnFinished(e ->  controlsVisible = true);

        // Transitions for the closeButton
        if (closeButton != null){
            fadeOutTransitionCloseButton = new FadeTransition(Duration.millis(FADE_DURATION_MILLIS), closeButton);
            fadeOutTransitionCloseButton.setFromValue(1.0);
            fadeOutTransitionCloseButton.setToValue( 0.0);

            fadeInTransitionCloseButton = new FadeTransition(Duration.millis(FADE_DURATION_MILLIS), closeButton );
            fadeInTransitionCloseButton.setFromValue( 0.0);
            fadeInTransitionCloseButton.setToValue(1.0);
        }

        hideControlsTimer = new Timeline(new KeyFrame(
                Duration.seconds(HIDE_DELAY_SECONDS),
                e -> hideControls()
        ));

        setupMouseActivityListeners();
        resetHideTimer();
    }

    private void setupMouseActivityListeners(){
        root.setOnMouseMoved( this::onUserActivity);
        root.setOnMouseClicked(this::onUserActivity );
        playerContainer.setOnMouseMoved( this::onUserActivity);
        playerContainer.setOnMouseClicked(this::onUserActivity);

        btnPlay.setOnMouseEntered(e ->  showControls());
        btnReturn.setOnMouseEntered( e -> showControls());
        btnForward.setOnMouseEntered(e ->  showControls());
        btnSpeed.setOnMouseEntered(e -> showControls());
        btnMaximize.setOnMouseEntered(e ->  showControls());
        volumeIconContainer.setOnMouseEntered(e ->  showControls());
        volumeSliderContainer.setOnMouseEntered( e ->  showControls());
        sliderVideo.setOnMouseEntered( e -> showControls());
        sliderVolume.setOnMouseEntered(e -> showControls());

        if (closeButton != null)
            closeButton.setOnMouseEntered(e ->  showControls() );

    }

    private void onUserActivity(MouseEvent event){
        showControls();
         resetHideTimer();
    }

    private void  showControls(){
        if (!controlsVisible){
            // bottomBar
            if (fadeOutTransition.getStatus() == javafx.animation.Animation.Status.RUNNING )
                fadeOutTransition.stop();

            fadeInTransition.play();

            // closeButton
            if (closeButton != null && fadeInTransitionCloseButton != null  && fadeOutTransitionCloseButton != null ){
                if (fadeOutTransitionCloseButton.getStatus() ==  javafx.animation.Animation.Status.RUNNING )
                    fadeOutTransitionCloseButton.stop();

                fadeInTransitionCloseButton.play();
            }
        }
        resetHideTimer();
    }

    private void hideControls(){
        if ( controlsVisible && mediaPlayer != null && mediaPlayer.getStatus() ==  MediaPlayer.Status.PLAYING){

            // bottomBar
            if ( fadeInTransition.getStatus()  == javafx.animation.Animation.Status.RUNNING)
                fadeInTransition.stop();

            fadeOutTransition.play(); // This transition's onFinished will set controlsVisible = false

            // closeButton
            if (closeButton  != null && fadeInTransitionCloseButton != null && fadeOutTransitionCloseButton != null){
                if (fadeInTransitionCloseButton.getStatus() ==  javafx.animation.Animation.Status.RUNNING)
                    fadeInTransitionCloseButton.stop();

                fadeOutTransitionCloseButton.play();
            }
        }
    }

    private void resetHideTimer(){
        hideControlsTimer.stop();
        if (mediaPlayer != null && mediaPlayer.getStatus() ==  MediaPlayer.Status.PLAYING)
             hideControlsTimer.play();
    }

    private void stopHideTimer() {
        hideControlsTimer.stop() ;
    }

    public void initializePlayer(String videoUrl,  User user, Account account){

        this.videoUrl = videoUrl;
        this.user = user;
        this.account = account;
        try{
            Media media = new Media(new URL(videoUrl).toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView( mediaPlayer);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(playerContainer.widthProperty());
            mediaView.fitHeightProperty().bind(playerContainer.heightProperty() );

            playerContainer.getChildren().addFirst(mediaView );
            if (closeButton  != null) closeButton.setOpacity(1.0);
            bottomBar.setOpacity(1.0);

            setupMediaPlayerListeners();

            mediaPlayer.setOnError(() ->
                    System.err.println("FilmPlayer: Video playback error: " + mediaPlayer.getError()));

            mediaPlayer.setVolume(sliderVolume.getValue() / 100.0);

        } catch (Exception e){
            System.err.println("FilmPlayer: Error creating video player: " + e.getMessage() );
        }
    }

    private void setupControlActions(){
        btnPlay.setOnMouseClicked(e -> {
            if (mediaPlayer  != null){
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING){
                    mediaPlayer.pause();
                    iconPlay.setContent(Main.resourceBundle.getString("buttons.play"));
                    stopHideTimer();
                    showControls();
                } else{
                    mediaPlayer.play();
                    iconPlay.setContent(Main.resourceBundle.getString("buttons.stop"));
                    resetHideTimer();
                }
            }
        });

        btnReturn.setOnMouseClicked(e ->{
            if (mediaPlayer   != null && mediaPlayer.getCurrentTime() !=  null ){
                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration newTime = currentTime.subtract(Duration.seconds(SKIP_DURATION) );
                if (newTime.lessThan(Duration.ZERO))
                    newTime = Duration.ZERO;
                mediaPlayer.seek(newTime);
                resetHideTimer();
            }
        });

        btnForward.setOnMouseClicked(e ->{
            if (mediaPlayer  != null && mediaPlayer.getCurrentTime() != null && mediaPlayer.getTotalDuration()   != null){
                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration totalTime = mediaPlayer.getTotalDuration();
                Duration newTime  = currentTime.add(Duration.seconds(SKIP_DURATION));

                if (newTime.greaterThan(totalTime) ){
                    newTime = totalTime;
                }

                mediaPlayer.seek(newTime);
                resetHideTimer();
            }
        });

        sliderVolume.valueProperty().addListener((obs,  oldVal, newVal) ->{
            if (mediaPlayer  != null){
                mediaPlayer.setVolume(newVal.doubleValue() /  100.0);
                updateVolumeIcon(newVal.doubleValue());
                resetHideTimer();
                if(isMuted){
                    isMuted= false;
                    updateVolumeIcon(sliderVolume.getValue());
                }
            }
        });

        volumeIconContainer.setOnMouseClicked(e ->{
            if (mediaPlayer  !=  null){
                if (isMuted){
                    mediaPlayer.setVolume(previousVolume);
                    sliderVolume.setValue(previousVolume * 100);
                    isMuted = false;
                } else{
                    previousVolume = mediaPlayer.getVolume();
                    mediaPlayer.setVolume(0);
                    sliderVolume.setValue(0);
                    isMuted  =  true;
                }
                updateVolumeIcon(sliderVolume.getValue());
                resetHideTimer();
            }
        });

        sliderVideo.setOnMousePressed(e ->  {
            if (mediaPlayer  != null && mediaPlayer.getTotalDuration()  !=  null){
                stopHideTimer();
                showControls();
                Duration seekTime = mediaPlayer.getTotalDuration().multiply(sliderVideo.getValue() / 100.0);
                mediaPlayer.seek(seekTime);
            }
        });

        sliderVideo.setOnMouseDragged(e ->{
            if (mediaPlayer  != null  && mediaPlayer.getTotalDuration() != null){
                showControls();
                Duration seekTime =  mediaPlayer.getTotalDuration().multiply(sliderVideo.getValue() / 100.0);
                mediaPlayer.seek(seekTime);
            }
        });

        sliderVideo.setOnMouseReleased(e -> resetHideTimer());

        btnSpeed.setOnMouseClicked(e ->{
            if (mediaPlayer != null){
                currentSpeedIndex = (currentSpeedIndex + 1) % playbackSpeeds.length;
                double newSpeed = playbackSpeeds[currentSpeedIndex];
                mediaPlayer.setRate(newSpeed);
                updateSpeedDisplay(newSpeed);
                resetHideTimer();
            }
        });



        btnMaximize.setOnMouseClicked(e ->{
            toggleFullscreen();
            resetHideTimer();
        });
    }


    private void setupMediaPlayerListeners(){
        if (mediaPlayer==null)
            return;

        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            textTotalTime.setText( formatTime(totalDuration));
            sliderVideo.setMax(100); // the percentage of the video
            showControls();
            if (mediaPlayer.getStatus()  != MediaPlayer.Status.PLAYING )
                 stopHideTimer(); // If not playing, the controls should not disappear
            else
                resetHideTimer();
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) ->
                Platform.runLater(() ->{
            textActualTime.setText(formatTime(newTime));
            if (mediaPlayer.getTotalDuration()  != null && mediaPlayer.getTotalDuration().toMillis() > 0){
                double progress = newTime.toMillis() / mediaPlayer.getTotalDuration().toMillis() * 100;
                if (!sliderVideo.isPressed()) // Only update if user is not dragging the slider
                    sliderVideo.setValue(progress);
            } else if (mediaPlayer.getTotalDuration() != null  &&  mediaPlayer.getTotalDuration().isUnknown() )
                sliderVideo.setDisable(true );// Handle live streams or unknown duration  - slider might not be applicable or behave differently , Example: disable slider for unknown duration
             else
                sliderVideo.setValue(0);
        }));

        mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() ->{
            iconPlay.setContent(Main.resourceBundle.getString( "buttons.play"));
            sliderVideo.setValue ( mediaPlayer.getTotalDuration() != null && !mediaPlayer.getTotalDuration().isUnknown()   ? 100  :  0);
            textActualTime.setText(formatTime(mediaPlayer.getTotalDuration()));
            stopHideTimer();
            showControls();
        }));

        mediaPlayer.setOnPlaying(() -> Platform.runLater(() ->{
            iconPlay.setContent(Main.resourceBundle.getString("buttons.stop" ));
            resetHideTimer();
        }));

        mediaPlayer.setOnPaused(() -> Platform.runLater( () ->{
            iconPlay.setContent( Main.resourceBundle.getString("buttons.play" ));
            stopHideTimer();
            showControls();
        }));

        mediaPlayer.setOnStopped(() -> Platform.runLater(() ->{
            iconPlay.setContent(Main.resourceBundle.getString("buttons.play"));
            sliderVideo.setValue(0);
            textActualTime.setText("00:00");
            stopHideTimer();
            showControls();
        }));
    }


    private void updateVolumeIcon(double volume){
        if (isMuted || volume <= 0)
            iconVolume.setContent(Main.resourceBundle.getString( "buttons.volume_mute"));
        else if (volume < 50)
            iconVolume.setContent(Main.resourceBundle.getString("buttons.volume_low" ));
        else
            iconVolume.setContent(Main.resourceBundle.getString("buttons.volume_high" ));

    }

    private void updateSpeedDisplay(double speed){
        if (speed == 0.5)
            iconSpeed.setContent(Main.resourceBundle.getString("buttons.speed_05"));
        else if  (speed == 0.7)
            iconSpeed.setContent(Main.resourceBundle.getString( "buttons.speed_07"));
        else if (speed == 1.0)
            iconSpeed.setContent(Main.resourceBundle.getString("buttons.speed_1"));
        else if ( speed == 1.2)
            iconSpeed.setContent(Main.resourceBundle.getString( "buttons.speed_12"));
        else if (speed == 1.5)
            iconSpeed.setContent(Main.resourceBundle.getString("buttons.speed_15"));
        else if ( speed == 2.0)
            iconSpeed.setContent(Main.resourceBundle.getString( "buttons.speed_2"));
        else{
            iconSpeed.setContent(Main.resourceBundle.getString(  "buttons.speed_1"));
        }
    }

    private void toggleFullscreen( ){
        isMaximized = !isMaximized;
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setFullScreen( isMaximized);
        iconMaximize.setContent( Main.resourceBundle.getString( isMaximized ? "buttons.minimize"   :  "buttons.maximize"));
    }

    private String formatTime(Duration duration){
        if (duration == null || duration.isUnknown()) return "00:00";

        long totalSeconds = (long) duration.toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void play(){
        if (mediaPlayer != null)
            mediaPlayer.play();

    }

    public void pause(){
        if (mediaPlayer  != null)
            mediaPlayer.pause();
    }

    public void stop(){
        if (mediaPlayer  != null)
            mediaPlayer.stop();
    }

    public void setVolume(double volume){
        if (mediaPlayer != null){
            mediaPlayer.setVolume(volume);
            sliderVolume.setValue(volume * 100);
            if (volume == 0 && !isMuted){
                previousVolume = mediaPlayer.getVolume();
                isMuted = true;
            } else if (volume > 0 && isMuted)
                isMuted = false;

            updateVolumeIcon(sliderVolume.getValue());
        }
    }

    public void seek(Duration time){
        if (mediaPlayer  != null){
            mediaPlayer.seek(time);
        }
    }

    public MediaPlayer.Status getStatus(){
          return mediaPlayer != null ? mediaPlayer.getStatus()  :  null;
    }

    public Duration getCurrentTime(){
        return mediaPlayer != null ? mediaPlayer.getCurrentTime()  :   null;
    }

    public Duration getTotalDuration(){
        return mediaPlayer != null ? mediaPlayer.getTotalDuration()  :   null;
    }

    public void returnToMainPages(){
        try{
            FXMLLoader loader =  new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"),Main.resourceBundle);
            Parent homeContent = loader.load();

            MainPagesController mainPagesController = loader.getController();
            mainPagesController.load(user,account);
            Scene currentScene =  root.getScene();
            this.dispose();
            Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage)  root.getScene().getWindow();
            stage.setScene(newScene);

        }catch(IOException e){
            System.err.println("FilmPlayer: Error to load the home page"+e.getMessage()) ;
        }
    }

    public void dispose(){
        if (hideControlsTimer != null)
            hideControlsTimer.stop();

        if (fadeOutTransition != null)
            fadeOutTransition.stop();

        if (fadeInTransition != null)
            fadeInTransition.stop();

        // Stop closeButton transitions
        if (fadeOutTransitionCloseButton != null)
            fadeOutTransitionCloseButton.stop();

        if (fadeInTransitionCloseButton != null)
            fadeInTransitionCloseButton.stop();

        if (mediaPlayer != null){
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        if (mediaView != null)
            mediaView.setMediaPlayer(null);

    }

}
