package com.github.dev.williamg.simplecouchbaseapp;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by wgutierrez on 2/9/17.
 */

public class MyCouchBase {

    //=== EDIT THESE TO ADAPT TO YOUR COUCHBASE INSTALLATION ===
    public static final String bucketName = "beer-sample";
    public static final String bucketPassword = "randpass1";
    public static final List<String> nodes = Arrays.asList("127.0.0.1");
    Context context;

    Manager manager;
    Database database;
    private String TAG = "MyCouchBase =>";

    public MyCouchBase(Context context) {
        this.context = context;
        DatabaseOptions option = new DatabaseOptions();
        option.setCreate(true);
        try {
            manager = new Manager(new AndroidContext(context.getApplicationContext()), Manager.DEFAULT_OPTIONS);
            database = manager.getExistingDatabase(bucketName);
            Map<String, Object> documents = database.getAllDocs(new QueryOptions());
            Log.d(TAG, "Connected to the cluster, Bucket: " + bucketName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();

        }
        try {
            startReplications();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }


    public void printValues() {
        for (String db :
                manager.getAllDatabaseNames()) {
            Log.d(TAG, "printValues: " + db);
        }

    }

    private void startReplications() throws CouchbaseLiteException {
        if (database != null) {
            //Replication pull = database.createPullReplication(createSyncURL(false));
            Replication push = database.createPushReplication(createSyncURL(false));
            //pull.setContinuous(true);
            push.setContinuous(true);
            //pull.start();
            push.start();
        }
    }

    private URL createSyncURL(boolean isEncrypted){
        URL syncURL = null;
        String host = "http://127.0.0.1";
        String port = "8091";
        String dbName = "beer-sample";
        try {
            syncURL = new URL(host + ":" + port + "/" + dbName);
        } catch (MalformedURLException me) {
            me.printStackTrace();
        }
        return syncURL;
    }

    public void close() {
        manager.close();
    }
}
