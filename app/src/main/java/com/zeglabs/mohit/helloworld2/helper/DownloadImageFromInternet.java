package com.zeglabs.mohit.helloworld2.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.Serializable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mohit on 31/8/16.
 */
public class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> implements Serializable{
    private static final long serialVersionUID = 1L;
    Context context = null;
    CircleImageView imageView;
    Bitmap image = null;

    public DownloadImageFromInternet(Context context, CircleImageView imageView) {
        this.context = context;
        this.imageView = imageView;
        Toast.makeText(context, "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();
    }

    public DownloadImageFromInternet(Context context, CircleImageView imageView, Bitmap image) {
        this.context = context;
        this.imageView = imageView;
        this.image = image;
        imageView.setImageBitmap(this.image);
    }

    public Bitmap doInBackground(String... urls) {
        String imageURL = urls[0];
        Bitmap bimage = null;
        try {
            InputStream in = new java.net.URL(imageURL).openStream();
            bimage = BitmapFactory.decodeStream(in);

        } catch (Exception e) {
            Log.e("Error Message", e.getMessage());
            e.printStackTrace();
        }
        this.image = bimage;
        return bimage;
    }

    protected void onPostExecute(Bitmap result) {
        PrefManager.addBitmapToMemoryCache(PrefManager.BITMAP_PHOTO_KEY, this.image);
        imageView.setImageBitmap(result);
    }

    public Bitmap getImage() {
        return this.image;
    }

    public void setImage(Bitmap result) { imageView.setImageBitmap(result); }
}
