package com.example.musicapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    private boolean isPlaying = true;

    // UI Elements
    TextView textView, startTime, endTime;
    ImageView play, previous, next;
    SeekBar seekBar;

    // Media components
    MediaPlayer mediaPlayer;
    ArrayList<File> songs;
    int position;
    String textContent;

    // Thread to update seek bar and start time
    Thread updateseek;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop updating UI and release media resources
        isPlaying = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (updateseek != null && updateseek.isAlive()) {
            updateseek.interrupt();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_song);

        // Binding views
        textView = findViewById(R.id.textView2);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);

        textView.setSelected(true); // Enables marquee effect

        // Receive intent data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songlist");
        textContent = intent.getStringExtra("CurrentSong");
        position = intent.getIntExtra("position", 0);

        // Set song name
        textView.setText(textContent);

        // Create MediaPlayer and start
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();

        // Set max seekbar and end time
        seekBar.setMax(mediaPlayer.getDuration());
        endTime.setText(formatTime(mediaPlayer.getDuration()));
        restartSeekbarUpdater();
        // SeekBar change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        // Thread to update seek bar and current time

        // Play/Pause toggle
        play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                play.setImageResource(R.drawable.play);
                mediaPlayer.pause();
            } else {
                play.setImageResource(R.drawable.pause);
                mediaPlayer.start();
            }
        });

        // Previous button click
        previous.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            position = (position != 0) ? position - 1 : songs.size() - 1;
            Uri newUri = Uri.parse(songs.get(position).toString());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), newUri);
            mediaPlayer.start();

            // Update UI
            play.setImageResource(R.drawable.pause);
            seekBar.setMax(mediaPlayer.getDuration());
            textView.setText(songs.get(position).getName());
            endTime.setText(formatTime(mediaPlayer.getDuration()));
            mediaPlayer.start();

            // Restart the thread for seekbar and time updates
            restartSeekbarUpdater();
        });
//next buttonn
        next.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            position = (position != songs.size() - 1) ? position + 1 : 0;
            Uri newUri = Uri.parse(songs.get(position).toString());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), newUri);
            mediaPlayer.start();

            // Update UI
            play.setImageResource(R.drawable.pause);
            seekBar.setMax(mediaPlayer.getDuration());
            textView.setText(songs.get(position).getName());
            endTime.setText(formatTime(mediaPlayer.getDuration()));
            mediaPlayer.start();
            // Restart the thread for seekbar and time updates
            restartSeekbarUpdater();
        });
    }
    private void restartSeekbarUpdater() {
        isPlaying = false;
        if (updateseek != null && updateseek.isAlive()) {
            updateseek.interrupt();
        }

        isPlaying = true;
        updateseek = new Thread(() -> {
            try {
                while (isPlaying && mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    runOnUiThread(() -> {
                        seekBar.setProgress(currentPosition);
                        startTime.setText(formatTime(currentPosition));
                    });
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        updateseek.start();
    }



    // Format milliseconds to MM:SS
    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Helper to stop, release and start new song
    private void playNewSong(int newPosition) {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = newPosition;
        Uri newUri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), newUri);
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause);

        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
        startTime.setText("00:00");
        endTime.setText(formatTime(mediaPlayer.getDuration()));
        textView.setText(songs.get(position).getName());
    }
}
