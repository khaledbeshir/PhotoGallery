package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.SearchView.OnQueryTextListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

/**
 * Created by Mohamed Amr on 5/8/2019.
 */

public class PhotoGalleryFragment extends visibleFragment{


    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static Fragment NewInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownloader= new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.SetThumbnailDownloanListener(
                new ThumbnailDownloader.ThumbnailDownloanListener<PhotoHolder>() {
                    @Override
                    public void OnThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources() , thumbnail);
                        target.bindGalleryItem(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG , "Background Thread started");
  }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery , container , false);
        mRecyclerView =(RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycle_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity() , 3));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG , "BackGround Thread Destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    public void SetUpAdapter (){
        if(isAdded())
        mRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }

    public class PhotoHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        private ImageView mItemImageView ;
        private GalleryItem mGalleryItem;
        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindGalleryItem(Drawable drawable){
           mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem1 (GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }
        @Override
        public void onClick(View view) {
            Intent intent = PagePhotoActivity.newIntent(getActivity(),mGalleryItem.getPhotoPageUri());
            startActivity(intent);
        }
    }

    public class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mItems;

        public PhotoAdapter(List<GalleryItem> items){
            mItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item , parent , false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mItems.get(position);
            holder.bindGalleryItem1(item);
            Drawable placeholder = getResources().getDrawable(R.drawable.ic_launcher_background);
            holder.bindGalleryItem(placeholder);
            mThumbnailDownloader.queueThumbinal(holder ,item.getUrl());

          //  Picasso.with(getActivity()).load(item.getUrl()).placeholder(R.drawable.ic_launcher_background).into(holder.mItemImageView);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void ,List<GalleryItem>>{

        String mQuery;
        public FetchItemsTask(String query){
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {

            if (mQuery == null){
                return new FlickerFitcher().fetchRecentPhotos();
            }
            else {

                return new FlickerFitcher().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            SetUpAdapter();
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallary , menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_poll);

        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        }else {
            toggleItem.setTitle(R.string.start_polling);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit" + s);
                QueryPreferences.setStoredQuery(getActivity(),s);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG ,"QueryTextChange" + s);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear :
                QueryPreferences.setStoredQuery(getActivity() , null);
                updateItems();
                return true;

            case R.id.menu_item_toggle_poll :
                boolean shouldStartAlarm  = !PollService.isServiceAlarmOn(getActivity());
                Log.i(TAG , "shouldStartAlarm equal " + shouldStartAlarm);
                PollService.setServiceAlarm(getActivity() , shouldStartAlarm );
                getActivity().invalidateOptionsMenu();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
    }
}
