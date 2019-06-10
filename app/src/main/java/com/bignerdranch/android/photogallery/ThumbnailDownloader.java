package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Target;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Mohamed Amr on 5/13/2019.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponeHandler;
    private ThumbnailDownloanListener mThumbnailDownloanListener;

    public interface ThumbnailDownloanListener<T> {
        void OnThumbnailDownloaded(T target , Bitmap thumbnail);
    }

    public void SetThumbnailDownloanListener (ThumbnailDownloanListener<T> listener){
        mThumbnailDownloanListener = listener;
    }

    public ThumbnailDownloader(Handler responehandler) {
        super(TAG);
        mResponeHandler =responehandler;
    }

    public void queueThumbinal (T target,String url){
        Log.i(TAG , "URL Got is : " + url);
        if(url == null){
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target , url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD , target)
                    .sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T target = (T)msg.obj;
                    Log.i(TAG ,"Got a Request for URL:" + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target){

        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickerFitcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes , 0,bitmapBytes.length);
            Log.i(TAG ,"Bitmap Created");
            mResponeHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target)!=url){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloanListener.OnThumbnailDownloaded(target , bitmap);
                }
            });
        }
        catch (IOException ioe){
            Log.e(TAG , "Error Downloading image" ,ioe);
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

}
