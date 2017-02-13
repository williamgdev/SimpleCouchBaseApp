package com.github.dev.williamg.simplecouchbaseapp;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.auth.AuthenticatorFactory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wgutierrez on 2/9/17.
 */

public class MyCouchBase {

    private String TAG = "MyCouchBase =>";

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    URL url;
//    public static final String stringURL = "http://192.168.1.155:4985/beer-sample";
    public static final String stringURL = "http://192.168.43.212:4984/beer-sample";
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
            database = manager.getExistingDatabase("beer-sample");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        saveMovie(manager, database, "123");
        retrieveMovie(manager, database, "123");
//        registerViews();

        continuousReplications();

//        startListener();

//        printValues();
    }

    /**
     * Start the Couchbase Lite Listener without any credentials for this demo.
     */
    private void startListener() {
        LiteListener listener = new LiteListener(manager, 50000, new Credentials("couchbase_user", "mobile"));

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
//        syncGatewaypush.setAuthenticator(AuthenticatorFactory.createBasicAuthenticator("couchbase_user", "mobile"));
        syncGatewaypush.setContinuous(true);
        syncGatewaypush.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                // will be called back when the push replication status changes
                switch (syncGatewaypush.getStatus()) {
                    case REPLICATION_STOPPED:
                        Log.d(TAG, "changed: PUSH STOOPED:" + syncGatewaypush.getStatus());
                        break;
                    case REPLICATION_OFFLINE:
                        Log.d(TAG, "changed: PUSH OFFLINE:" + syncGatewaypush.getStatus());
                        break;
                    case REPLICATION_IDLE:
                        Log.d(TAG, "changed: PUSH IDLE:" + syncGatewaypush.getStatus());
                        break;
                    case REPLICATION_ACTIVE:
                        Log.d(TAG, "changed: PUSH Active:" + syncGatewaypush.getStatus());
                        break;
                }
            }
        });
        syncGatewaypush.start();
        Log.d(TAG, "continuousReplications: Push Start");

        syncGatewaypull = database.createPullReplication(url);
//        syncGatewaypull.setAuthenticator(AuthenticatorFactory.createBasicAuthenticator("couchbase_user", "mobile"));
        syncGatewaypull.setContinuous(true);

        syncGatewaypull.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                // will be called back when the pull replication status changes
                 switch (syncGatewaypull.getStatus()) {
                     case REPLICATION_STOPPED:
                         Log.d(TAG, "changed: PULL STOOPED:" + syncGatewaypull.getStatus());
                         break;
                     case REPLICATION_OFFLINE:
                         Log.d(TAG, "changed: PULL OFFLINE:" + syncGatewaypull.getStatus());
                         break;
                     case REPLICATION_IDLE:
                         Log.d(TAG, "changed: PULL IDLE:" + syncGatewaypull.getStatus());
                         break;
                     case REPLICATION_ACTIVE:
                         Log.d(TAG, "changed: PULL Active:" + syncGatewaypull.getStatus());
                         break;
                 }
            }
        });
        syncGatewaypull.start();
        Log.d(TAG, "continuousReplications: Pull Start");

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

    private void saveMovie(Manager manager, Database couchDb, String docId) {
        Map<String, Object> docContent = new HashMap<String, Object>();
        docContent.put("message", "Hey Couchbase Lite");
        docContent.put("data", "William Gutierrez Torriente.");
        Log.i("saveMovie", "docContent=" + String.valueOf(docContent));

        // create an empty document, add content and write it to the couchDb
        Document document = new Document(couchDb, docId);
        try {
            document.putProperties(docContent);
            Log.d(TAG, "saveMovie: " + "Document written to couchDb named " + couchDb + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.i("saveMovie", "Failed to write document to Couchbase database!");
        }
    }

    private void retrieveMovie(Manager manager, Database couchDb, String docId) {
        Document retrievedDocument = couchDb.getDocument(docId); // Retrieve the document by id
        String data = (String)retrievedDocument.getProperties().get("data");


        Log.i("TAG", "jsonString>>>" + data);
    }

}
