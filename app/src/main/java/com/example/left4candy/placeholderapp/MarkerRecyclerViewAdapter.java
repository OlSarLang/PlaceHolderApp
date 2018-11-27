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

    private ArrayList<String> headerList = new ArrayList<>();
    private ArrayList<String> subHeaderOneList = new ArrayList<>();
    private ArrayList<String> subHeaderTwoList =  new ArrayList<>();
    private Context context;

    public MarkerRecyclerViewAdapter(ArrayList<String> headerList, ArrayList<String> subHeaderOneList, ArrayList<String> subHeaderTwoList, Context context) {
        this.headerList = headerList;
        this.subHeaderOneList = subHeaderOneList;
        this.subHeaderTwoList = subHeaderTwoList;
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
        holder.header.setText(headerList.get(position));
        holder.subHeaderOne.setText(subHeaderOneList.get(position));
        holder.subHeaderTwo.setText(subHeaderTwoList.get(position));
    }

    @Override
    public int getItemCount() {
        return headerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView id;
        EditText header;
        EditText subHeaderOne;
        EditText subHeaderTwo;
        RelativeLayout itemParentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            id = itemView.findViewById(R.id.idTextView);
            header = itemView.findViewById(R.id.editText0);
            subHeaderOne = itemView.findViewById(R.id.editText1);
            subHeaderTwo = itemView.findViewById(R.id.editText2);
        }
    }
}
