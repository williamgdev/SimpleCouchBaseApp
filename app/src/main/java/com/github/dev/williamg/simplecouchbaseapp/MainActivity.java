package com.github.dev.williamg.simplecouchbaseapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.QueryRow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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

//
//        LiveQuery liveQuery = myCouchBase.database.getView(MyCouchBase.VIEW_BREWERY_BEERS)
//                .createQuery()
//                .toLiveQuery();
//        liveQuery.setGroupLevel(1);
//        liveQuery.setDescending(true);


        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager( new LinearLayoutManager(this));
        documentAdapter = new DocumentAdapter(myCouchBase.getAllDocumentsId(), this);
        recyclerView.setAdapter(documentAdapter);
//        myCouchBase.close();

    }

    public void onGO(View view) {

        myCouchBase.saveDocument(editText.getText().toString());
        documentAdapter.resetDocs(myCouchBase.getAllDocumentsId());
        recyclerView.setAdapter(documentAdapter);
    }
}
