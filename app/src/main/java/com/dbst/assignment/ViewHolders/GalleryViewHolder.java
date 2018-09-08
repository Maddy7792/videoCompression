package com.dbst.assignment.ViewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dbst.assignment.R;
import com.dbst.assignment.Utils.FileManager;
import com.dbst.assignment.models.Images;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static android.support.constraint.Constraints.TAG;

public class GalleryViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivGallery;
    //private static FutureTarget<Bitmap> theBitmap = null;
    private static Bitmap theBitmap = null;
    public GalleryViewHolder(View itemView) {
        super(itemView);
        ivGallery = itemView.findViewById(R.id.image_gallery);
    }

    public void onBind(ArrayList<Images> images, int position, final Context context) {
        if (images.get(position).getImage() != null) {
            Glide.with(context).asBitmap().load(images.get(position).getImage()).into(ivGallery);
            new DownloadTask(context).execute(images.get(position).getImage());
        }

    }

    public static class DownloadTask extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        private Context context;
        private Target target;
        private String list;

        private DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {

            if (Looper.myLooper() == null){
                Looper.prepare();
            }
            list = strings[0];
            Log.d("Download-images",""+strings[0]);
            try {
                theBitmap = Glide.with(context).asBitmap().load(strings[0]).submit().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (theBitmap != null) {
                Log.d("Download-Bitmap", "" + theBitmap);
                String random = UUID.randomUUID().toString();
                try {
                    FileManager.saveImages(context,theBitmap,list,FileManager.getDirectoryName("DBSTImages"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
