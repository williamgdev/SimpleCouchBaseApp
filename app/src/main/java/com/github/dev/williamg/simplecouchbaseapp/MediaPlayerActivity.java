package com.github.dev.williamg.simplecouchbaseapp;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MediaPlayerActivity extends AppCompatActivity {
    TextView txtTitle;
    Button bClose;
    MediaPlayer mediaPlayer;
    private String TAG = "MediaPlayerActivity =>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        txtTitle = (TextView) findViewById(R.id.player_txt_title);
        bClose = (Button) findViewById(R.id.player_button_close);

        /**
         * Chnage this code bellow when you are getting the audio file from the Database
         */
        Log.d(TAG, "onCreate: media url: " + Uri.parse(getIntent().getStringExtra("music")));
        mediaPlayer = MediaPlayer.create(this, Uri.parse(getIntent().getStringExtra("music")));
        mediaPlayer.start();

    }


    public void onPlayerClose(View view) {
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();
        this.finish();
    }
}
