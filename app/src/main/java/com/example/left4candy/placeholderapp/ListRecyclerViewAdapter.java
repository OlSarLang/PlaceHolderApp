package com.example.left4candy.placeholderapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ListRecyclerViewAdapter extends RecyclerView.Adapter<ListRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "ListRecyclerViewAdapter";

    private List<CustomMarker> customMarkers;
    private Context context;

    public ListRecyclerViewAdapter(List<CustomMarker> customMarkers, Context context) {
        super();
        this.customMarkers = customMarkers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.id.setText(String.valueOf(customMarkers.get(position).getMarkerId()));
        holder.name.setText(String.valueOf(customMarkers.get(position).getMarkerName()));
        Log.d("listadapter", String.valueOf(customMarkers.get(position).getMarkerName()));
        Log.d("listadapter", String.valueOf(customMarkers.get(position).getMarkerId()));
    }

    @Override
    public int getItemCount() { return customMarkers.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView id;
        TextView name;
        Button itemButton;
        RelativeLayout itemParentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            id = itemView.findViewById(R.id.idText);
            name = itemView.findViewById(R.id.nameText);
            itemButton = itemView.findViewById(R.id.itemButton);
            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemClickListener != null){
                        mItemClickListener.onItemClickListener(v, getAdapterPosition());
                    }
                }
            });
        }
    }

    private ListRecyclerViewAdapter.onRecyclerViewItemClickListener mItemClickListener;
    public void setOnItemClickListener(ListRecyclerViewAdapter.onRecyclerViewItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener{
        void onItemClickListener(View view, int position);
    }
}
