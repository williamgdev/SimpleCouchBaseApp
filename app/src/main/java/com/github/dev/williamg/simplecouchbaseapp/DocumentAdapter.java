package com.github.dev.williamg.simplecouchbaseapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.util.List;

/**
 * Created by wgutierrez on 2/13/17.
 */

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    Context context;
    List<Document> documents;
    private String TAG = "DocumentAdapter";

    public DocumentAdapter(List<Document> documents, Context context){
        this.context = context;
        resetDocs(documents);
    }

    public void addMoreDocs(List<Document> documents){
        if(this.documents == null) {
            this.documents = documents;
        } else {
            this.documents.addAll(documents);
        }
        notifyDataSetChanged();
    }

    public void resetDocs(List<Document> documents){
        this.documents = documents;
        notifyDataSetChanged();
    }

    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_view, parent, false);
        return new DocumentAdapter.DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        final Document doc = getDocumentItem(position);
        holder.txtViewId.setText(doc.getId());
        if (doc.getProperty(MyCouchBase.DOCUMENT_KEY_DATA) != null){
            holder.txtViewName.setText(String.valueOf(doc.getProperty(MyCouchBase.DOCUMENT_KEY_DATA)));
        }

    }

    private Document getDocumentItem(int position) {
        return documents != null ?
                documents.get(position):
                null;
    }

    @Override
    public int getItemCount() {
        return documents != null ?
                documents.size() :
                0;
    }

    public class DocumentViewHolder extends RecyclerView.ViewHolder {

        TextView txtViewId, txtViewName;
        public DocumentViewHolder(View itemView) {
            super(itemView);
            txtViewId = (TextView) itemView.findViewById(R.id.item_text_view_id);
            txtViewName = (TextView) itemView.findViewById(R.id.item_txt_view_name);
        }
    }
}
