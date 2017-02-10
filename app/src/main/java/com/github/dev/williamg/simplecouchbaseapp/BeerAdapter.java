package com.github.dev.williamg.simplecouchbaseapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.w3c.dom.Text;

/**
 * Created by wgutierrez on 2/10/17.
 */
public class BeerAdapter extends RecyclerView.Adapter<BeerAdapter.BeerViewHolder> {
    Context context;
    LiveQuery liveQuery;
    private QueryEnumerator queryEnumerator;
    private String TAG = "BeerAdapter =>";

    public BeerAdapter(final LiveQuery liveQuery, Context context) {
        this.context = context;
        this.liveQuery = liveQuery;
        this.liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                ((Activity) BeerAdapter.this.context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        queryEnumerator = liveQuery.getRows();
                        notifyDataSetChanged();
                    }
                });
            }
        });
        liveQuery.start();
        Log.d(TAG, "BeerAdapter: LiveQuery Start");

    }

    public BeerAdapter(Context context, final LiveQuery query, Database database) {
        this.context = context;
        this.liveQuery = query;

        /** Use the database change listener instead of live query listener because we want
         * to listen for conflicts as well
         */
        database.addChangeListener(new Database.ChangeListener() {
            @Override
            public void changed(final Database.ChangeEvent event) {
                ((Activity) BeerAdapter.this.context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        queryEnumerator = query.getRows();
                        notifyDataSetChanged();
                    }
                });
            }
        });
        query.start();
        ((Activity) BeerAdapter.this.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    queryEnumerator = query.run();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public BeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_view, parent, false);
        return new BeerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BeerViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Showing Data");
        final QueryRow row = (QueryRow) getItem(position);
        holder.txtViewId.setText(String.valueOf(row.getKey()));
        if (row.getValue() != null){
            holder.txtViewName.setText(String.valueOf(row.getValue()));
        }

    }

    private Object getItem(int position) {
        return queryEnumerator != null ?
                queryEnumerator.getRow(position) :
                null;
    }

    @Override
    public int getItemCount() {
        return queryEnumerator != null ?
                queryEnumerator.getCount() :
                0;
    }

    public class BeerViewHolder extends RecyclerView.ViewHolder {
        TextView txtViewId, txtViewName;
        public BeerViewHolder(View view) {
            super(view);
            txtViewId = (TextView) view.findViewById(R.id.item_text_view_id);
            txtViewName = (TextView) view.findViewById(R.id.item_txt_view_name);
        }
    }
}
