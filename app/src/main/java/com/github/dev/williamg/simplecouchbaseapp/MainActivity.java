package com.github.dev.williamg.simplecouchbaseapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryRow;

import java.util.Iterator;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CouchBaseApp =>";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyCouchBase myCouchBase = new MyCouchBase(this);

        LiveQuery liveQuery = myCouchBase.database.getView(MyCouchBase.VIEW_BREWERY_BEERS)
                .createQuery()
                .toLiveQuery();
        liveQuery.setGroupLevel(1);
        liveQuery.setDescending(true);


        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager( new LinearLayoutManager(this));

        recyclerView.setAdapter(new BeerAdapter(liveQuery, this));
        //myCouchBase.close();
    }


}
