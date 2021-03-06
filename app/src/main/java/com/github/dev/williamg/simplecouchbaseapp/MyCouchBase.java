package com.github.dev.williamg.simplecouchbaseapp;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wgutierrez on 2/9/17.
 */

public class MyCouchBase {

    private String TAG = "MyCouchBase =>";

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    URL url;
//    public static final String stringURL = "http://192.168.1.154:4985/beer-sample"; //QANet
//    public static final String stringURL = "http://192.168.43.212:4984/beer-sample"; //MobileData
    public static final String stringURL = "http://10.200.248.70:4984/beer-sample"; //RandDevices

    public static final String VIEW_BREWERY_BEERS = "brewery_beers";
    public static String DOCUMENT_KEY_DATA = "data";

    Manager manager;
    Database database;
    Replication syncGatewaypull;
    Replication syncGatewaypush;
    Replication peerPull;
    Replication peerPush;

    public MyCouchBase(final Context context) {
        try {
            /** Enable logging in the application for all tags */
            Manager.enableLogging(TAG, Log.VERBOSE);
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            database = manager.getDatabase("beer-sample");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

//        saveMovie(manager, database, "123");
//        retrieveMovie(manager, database, "123");
//        registerViews();
        continuousReplications();
//        startListener();
//        printValues();
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

    }

    public void stopSyncGatewayReplication() {
        syncGatewaypull.stop();
        syncGatewaypush.stop();
    }

    public void startSyncGatewayReplication() {
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

    public void saveMovie(Manager manager, Database couchDb, String docId) {
        Map<String, Object> docContent = new HashMap<String, Object>();
        docContent.put("message", "Hey Couchbase Lite");
        docContent.put("data", "Wil Gutier.");
        Log.i("saveMovie", "docContent=" + String.valueOf(docContent));

        // create an empty document, add content and write it to the couchD
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
        String data = (String) retrievedDocument.getProperties().get("data");


        Log.i("TAG", "jsonString>>>" + data);
    }

    public void logAllDocuments() {
        try {
            for (Map.Entry<String, Object> doc :
                    database.getAllDocs(new QueryOptions()).entrySet()) {
                Log.d(TAG, "logAllDocuments: Name: " + doc.getValue().toString());
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            Log.e(TAG, "logAllDocuments: " + e.getMessage());
        }
    }

    public List<Document> getAllDocumentsId() {
        List<Document> documents = new ArrayList<>();
        QueryEnumerator queryEnumerator = null;
        try {
            database.getAllDocs(new QueryOptions());
            Query queryAllDocs = database.createAllDocumentsQuery();
            queryEnumerator = queryAllDocs.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        for (Iterator<QueryRow> it = queryEnumerator; it.hasNext(); ) {
            QueryRow row = it.next();
            Document document = row.getDocument();
            documents.add(document);
        }
        return documents;

    }

    public void saveDocument(String text) {
        Map<String, Object> docContent = new HashMap();
        docContent.put("message", "Hey Couchbase Lite, I'm a new document");
        docContent.put(DOCUMENT_KEY_DATA, text);
        Log.i(TAG, "docContent=" + String.valueOf(docContent));

        // create an empty document, add content and write it to the couchDb
        Document document = database.createDocument();
        try {
            document.putProperties(docContent);
            Log.d(TAG, "saveDocument: " + "Document written to couchDb named " + database + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.i(TAG, "saveDocument: Failed to write document to Couchbase database!");
        }
    }

    public void saveAttachment(String music, InputStream stream) {
        Document doc = database.getDocument(music);
        /**
         * If you do not have any music Revision
         * uncomment this code bellow
         */
        /*
        if (doc.getCurrentRevision() == null) {
            UnsavedRevision newRev = doc.createRevision();
            try {
                newRev.save();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
         */
        UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
        newRev.setAttachment("music.mp3", "audio/mpeg3", stream);
        try {
            newRev.save();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "saveAttachment: Error " + e.getMessage());
        }
    }

    public URL getMusicFileURL() {
        Document doc = database.getDocument("music");
        Revision rev = doc.getCurrentRevision();
        Attachment att = rev.getAttachment("music.mp3");
        return att.getContentURL();

    }

    public InputStream getMusicFile() {
        Document doc = database.getDocument("music");
        Revision rev = doc.getCurrentRevision();
        Attachment att = rev.getAttachment("music.mp3");
        InputStream music = null;
        if (att != null) {
            try {
                return att.getContent();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        return music;

    }
}
