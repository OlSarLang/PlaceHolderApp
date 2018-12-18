package com.example.left4candy.placeholderapp;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagePickerAdapter extends RecyclerView.Adapter<ImagePickerAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Upload> mUploads;

    public ImagePickerAdapter(Context context, List<Upload> uploads){
        mContext = context;
        mUploads = uploads;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        Picasso.get()
                .load(uploadCurrent.getMImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;

        public ImageViewHolder(View itemView){
            super(itemView);

            imageView = itemView.findViewById(R.id.image_upload);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemClickListener != null){
                        mItemClickListener.onItemClickListener(v, getAdapterPosition());
                    }
                }
            });
        }
    }

    private onRecyclerViewItemClickListener mItemClickListener;
    public void setOnImageClickListener(onRecyclerViewItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener{
        void onItemClickListener(View view, int position);
    }
}
