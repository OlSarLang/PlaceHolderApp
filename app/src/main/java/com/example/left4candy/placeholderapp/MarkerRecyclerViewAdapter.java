package com.example.left4candy.placeholderapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MarkerRecyclerViewAdapter extends RecyclerView.Adapter<MarkerRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "MarkerRecyclerViewAdapt";

    private ArrayList<MarkerItem> markerItems;
    private Context context;

    public MarkerRecyclerViewAdapter(ArrayList<MarkerItem> markerItems, Context context) {
        super();
        this.markerItems = markerItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.field_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.header.setText(markerItems.get(position).getHeader());
        holder.subHeaderOne.setText(markerItems.get(position).getSubHeader());
    }

    @Override
    public int getItemCount() {
        return markerItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        EditText header;
        EditText subHeaderOne;
        RelativeLayout itemParentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            header = itemView.findViewById(R.id.headertext);
            subHeaderOne = itemView.findViewById(R.id.subheadertext);
        }
    }
}
