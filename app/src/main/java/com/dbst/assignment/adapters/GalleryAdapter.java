package com.dbst.assignment.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dbst.assignment.R;
import com.dbst.assignment.ViewHolders.GalleryViewHolder;
import com.dbst.assignment.activites.GalleryActivity;
import com.dbst.assignment.models.Images;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder>{
    private Context context;
    private ArrayList<Images> images;

    public GalleryAdapter(Context galleryActivity) {
        this.context=galleryActivity;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item_layout,null);
        GalleryViewHolder viewHolder = new GalleryViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.onBind(images,position,context);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setData(ArrayList<Images> imagesList) {
        this.images = imagesList;
    }


}
