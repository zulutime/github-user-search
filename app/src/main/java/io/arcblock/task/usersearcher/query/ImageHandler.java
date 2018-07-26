package io.arcblock.task.usersearcher.query;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ImageHandler {

    private static final String TAG = ImageHandler.class.getSimpleName();
    private static final String LOG_TAG = ImageHandler.class.getSimpleName();
    private static final int MAX_PHOTO_SIZE = 64;

    public interface ImageFetcherCallback {
        void onImageAvailable(Bitmap bitmap);
    }

    public static void getAvatar(final String url, final ImageFetcherCallback callback) {
        Log.d(LOG_TAG, "download url " + url);
        Picasso.get().load(url).resize(MAX_PHOTO_SIZE, MAX_PHOTO_SIZE)
                .transform(new CropCircleTransformation()).into(
                new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        Log.d(LOG_TAG, "download done " + url);
                        callback.onImageAvailable(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e(LOG_TAG, "picasso error bitmap failed " + url, e);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
    }
}
