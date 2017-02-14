package com.github.dev.williamg.simplecouchbaseapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;

import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CouchBaseApp =>";
    RecyclerView recyclerView;
    MyCouchBase myCouchBase;
    EditText editText;
    private DocumentAdapter documentAdapter;

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
                        playMusic();
                    }
                }
                updateUI();
            }
        });

    }

    private void playMusic() {
        //Play Music here
        InputStream music = myCouchBase.getMusicFile();
        if (music != null)
            Log.d(TAG, "playMusic: Play Music");
    }

    public void onGO(View view) {
        if (editText.getText().toString().equals(""))
            loadFile();
        else
            myCouchBase.saveDocument(editText.getText().toString());
        updateUI();
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
