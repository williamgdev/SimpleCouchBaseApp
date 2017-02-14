package com.github.dev.williamg.simplecouchbaseapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;

import java.io.File;
import java.io.InputStream;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CouchBaseApp =>";
    RecyclerView recyclerView;
    MyCouchBase myCouchBase;
    EditText editText;
    private DocumentAdapter documentAdapter;
    private boolean isImSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myCouchBase = new MyCouchBase(this);

        editText = (EditText) findViewById(R.id.main_text);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        documentAdapter = new DocumentAdapter(myCouchBase.getAllDocumentsId(), this);
        recyclerView.setAdapter(documentAdapter);

        myCouchBase.database.addChangeListener(new Database.ChangeListener() {
            @Override
            public void changed(Database.ChangeEvent event) {
                for (DocumentChange change : event.getChanges()) {
                    if(change.getDocumentId().equals("music")){
                        //Receiving Music File
                        if (!isImSender)
                            playMusic(change.getSource());
                    }
                }
                updateUI();
            }
        });
    }

    private void playMusic(URL musicPath) {
        InputStream musicStream = myCouchBase.getMusicFile();

        File musicFile = new File(this.getFilesDir(), "music.mp3");
        FileUtil.copyInputStreamToFile(musicStream, musicFile);
//        mediaPlayer = MediaPlayer.create(this, Uri.parse(musicFile.getAbsolutePath()));
        if (musicFile != null) {
            Log.d(TAG, "playMusic: Play Music");
            /**
             * Find the way to send the FileStream to the MediaPlayerActivity
             *
             */
            Intent intent = new Intent(this, MediaPlayerActivity.class);
            intent.putExtra("music", musicFile.getAbsolutePath().toString());
            startActivity(intent);
        }
    }

    public void onGO(View view) {
        if (editText.getText().toString().equals(""))
            loadFile();
        else
            myCouchBase.saveDocument(editText.getText().toString());
        updateUI();
        isImSender = true;
    }

    private void updateUI() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                documentAdapter.resetDocs(myCouchBase.getAllDocumentsId());
                recyclerView.setAdapter(documentAdapter);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myCouchBase.stopSyncGatewayReplication();
        myCouchBase.close();
    }

    private void loadFile() {
        InputStream stream = getResources().openRawResource(R.raw.music);
        myCouchBase.saveAttachment("music", stream);
    }

}
