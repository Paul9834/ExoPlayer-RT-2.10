
package com.paul9834.exoiptv;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Reproducción de ExoPlayer a través de microservicio Rest
 *
 * @author  Kevin Paul Montealegre Melo
 * @version 1.0
 */


public class PlayerActivity extends AppCompatActivity   {

  private PlaybackStateListener playbackStateListener;
  private static final String TAG = PlayerActivity.class.getName();

  private PlayerView playerView;
  private SimpleExoPlayer player;
  private boolean playWhenReady = true;
  private int currentWindow = 0;
  private long playbackPosition = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    playerView = findViewById(R.id.video_view);

    playbackStateListener = new PlaybackStateListener();
  }


  @Override
  public void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      initializePlayer();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT <= 23 || player == null)) {
      initializePlayer();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  @Override
  protected void onUserLeaveHint() {
    super.onUserLeaveHint();


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        enterPictureInPictureMode(
                new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(350, 200))
                        .setSourceRectHint(new Rect(
                                playerView.getLeft(), playerView.getTop(),
                                playerView.getRight(), playerView.getBottom()))
                        .build());

    }
  }


  private void initializePlayer() {
    if (player == null) {
      DefaultTrackSelector trackSelector = new DefaultTrackSelector();
      trackSelector.setParameters(
              trackSelector.buildUponParameters().setMaxVideoSizeSd());
      player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
    }

    playerView.setPlayer(player);


    Uri uri = Uri.parse("http://45.172.222.2:9128/espn2hd");


    MediaSource mediaSource = buildMediaSource(uri);


    final LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);
    player.prepare(loopingSource);


    player.addListener(new ExoPlayer.EventListener() {
      @Override
      public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
      }

      @Override
      public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.v(TAG, "Listener-onTracksChanged... ");
      }

      @Override
      public void onLoadingChanged(boolean isLoading) {
      }

      @Override
      public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch(playbackState) {
          case ExoPlayer.STATE_BUFFERING:
            break;
          case ExoPlayer.STATE_ENDED:
            player.seekTo(0);
            break;
          case ExoPlayer.STATE_IDLE:
            break;
          case ExoPlayer.STATE_READY:
            break;
          default:
            break;
        }
  }
      @Override
      public void onRepeatModeChanged(int repeatMode) {
      }
      @Override
      public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      }
      @Override
      public void onPlayerError(ExoPlaybackException error) {


        switch (error.type) {
          case ExoPlaybackException.TYPE_SOURCE:
            Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());

            player.seekTo(0);
            player.prepare(loopingSource);
            player.setPlayWhenReady(true);

            break;

          case ExoPlaybackException.TYPE_RENDERER:
            Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
            player.seekTo(0);
            player.prepare(loopingSource);
            player.setPlayWhenReady(true);


            break;

          case ExoPlaybackException.TYPE_UNEXPECTED:
            Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
            player.seekTo(0);
            player.prepare(loopingSource);
            player.setPlayWhenReady(true);

            break;
        }

        }




      @Override
      public void onPositionDiscontinuity(int reason) {




      }
      @Override
      public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      }
      @Override
      public void onSeekProcessed() {
      }
    });

    player.seekTo(0);
    player.setPlayWhenReady(true);


  }

  private void releasePlayer() {
    if (player != null) {
      playbackPosition = player.getCurrentPosition();
      currentWindow = player.getCurrentWindowIndex();
      playWhenReady = player.getPlayWhenReady();
      player.removeListener(playbackStateListener);
      player.release();
      player = null;
    }
  }


  private MediaSource buildMediaSource(Uri uri) {

    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");

    ProgressiveMediaSource.Factory mediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);

    return mediaSourceFactory.createMediaSource(uri);


  }

  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }




  private class PlaybackStateListener implements Player.EventListener{

    @Override
    public void onPlayerStateChanged(boolean playWhenReady,
                                     int playbackState) {
      String stateString;
      switch (playbackState) {
        case ExoPlayer.STATE_IDLE:
          stateString = "ExoPlayer.STATE_IDLE      -";
          break;
        case ExoPlayer.STATE_BUFFERING:
          stateString = "ExoPlayer.STATE_BUFFERING -";
          break;
        case ExoPlayer.STATE_READY:
          stateString = "ExoPlayer.STATE_READY     -";
          break;
        case ExoPlayer.STATE_ENDED:
          stateString = "ExoPlayer.STATE_ENDED     -";
          break;
        default:
          stateString = "UNKNOWN_STATE             -";
          break;
      }
      Log.d(TAG, "changed state to " + stateString
              + " playWhenReady: " + playWhenReady);
    }
  }

  public void restartApp2() {

    Intent mStartActivity = new Intent(getApplicationContext(), PlayerActivity.class);
    int mPendingIntentId = 123456;
    PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
    System.exit(0);

  }

  public void restartApp () {

    Log.e("PAUL", "Paul");
    Intent i = new Intent(PlayerActivity.this, PlayerActivity.class);
    finish();
    overridePendingTransition(0, 0);
    startActivity(i);
    overridePendingTransition(0, 0);

  }

}
