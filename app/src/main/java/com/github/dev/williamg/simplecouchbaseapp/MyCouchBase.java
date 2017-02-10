package com.github.dev.williamg.simplecouchbaseapp;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.auth.PasswordAuthorizer;
import com.couchbase.lite.auth.TokenAuthenticator;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by wgutierrez on 2/9/17.
 */

public class MyCouchBase {

    private String TAG = "MyCouchBase =>";

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    URL url;
    public static final String stringURL = "http://127.0.0.1:4984/beer-sample";
    public static final String VIEW_BREWERY_BEERS = "brewery_beers";

    Manager manager;
    Database database;
    Replication syncGatewaypull;
    Replication syncGatewaypush;
    Replication peerPull;
    Replication peerPush;

    public MyCouchBase(Context context) {
        try {
            /** Enable logging in the application for all tags */
            Manager.enableLogging(TAG , Log.VERBOSE);
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            database = manager.getDatabase("ratingapp");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        registerViews();

        continuousReplications();

        startListener();
    }

    /**
     * Start the Couchbase Lite Listener without any credentials for this demo.
     */
    private void startListener() {
        LiteListener listener = new LiteListener(manager, 50000, new Credentials("", ""));

    }

    /**
     * Start push/pull replications with Sync Gateway.
     */
    private void continuousReplications() {
        Log.d(TAG, "continuousReplications: Starting");
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        syncGatewaypush = database.createPushReplication(url);
        syncGatewaypush.setAuthenticator(new PasswordAuthorizer("couchbase_user", "mobile"));
        syncGatewaypush.setContinuous(true);
        syncGatewaypush.start();

        syncGatewaypull = database.createPullReplication(url);
        syncGatewaypush.setAuthenticator(new PasswordAuthorizer("couchbase_user", "mobile"));
        syncGatewaypull.setContinuous(true);
        syncGatewaypull.start();

    }

    public void stopSyncGatewayReplication(){
        syncGatewaypull.stop();
        syncGatewaypush.stop();
    }

    public void startSyncGatewayReplication(){
        syncGatewaypull.start();
        syncGatewaypush.start();
    }

    /**
     * Register the views when the database is fist opened.
     */
    private void registerViews() {
        View breweryBeers = database.getView(VIEW_BREWERY_BEERS);
        breweryBeers.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if (document.get("type").equals("beer")) {
                    emitter.emit(document.get("name").toString(), null);
                    Log.d(TAG, "map: Emitting: " + document.get("name").toString());
                }
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                return new Integer(values.size());
            }
        }, breweryBeers.getMapVersion());
    }


    public void printValues() {
        for (String db :
                manager.getAllDatabaseNames()) {
            Log.d(TAG, "printValues: " + db);
        }

    }

    public void close() {
        manager.close();
    }
}
