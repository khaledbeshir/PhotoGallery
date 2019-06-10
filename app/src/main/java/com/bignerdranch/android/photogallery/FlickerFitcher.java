package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

/**
 * Created by Mohamed Amr on 5/10/2019.
 */

public class FlickerFitcher {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "9d339c448c1515355c46a17c51f29d5f";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/").
            buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public  byte[] getUrlBytes (String urlSpec) throws IOException {
        URL url1 = new URL(urlSpec);
        HttpURLConnection connection =(HttpURLConnection) url1.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() +
                        ":with " + urlSpec);
            }

            byte[] buffer = new byte[1024];
            int bytesread =  0;
            while ((bytesread = in.read(buffer)) >0){
                out.write(buffer , 0 ,bytesread);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }

    }

    public String getUrlString (String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos (){
        String url = buildUrl(FETCH_RECENTS_METHOD , null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query)
    {
        String url = buildUrl(SEARCH_METHOD , query);
        return downloadGalleryItems(url);
    }

    private String buildUrl (String method, String query){
        Uri.Builder UriBuilder = ENDPOINT.buildUpon().
                appendQueryParameter("method" , method);

        if (method.equals(SEARCH_METHOD)){
            UriBuilder.appendQueryParameter("text" , query);
        }
        return UriBuilder.build().toString();
    }

    private List<GalleryItem> downloadGalleryItems (String url){
        List<GalleryItem> items = new ArrayList<>();
        try{
            String jsonString = getUrlString(url);
            Log.i(TAG , "Recieved Json :" + jsonString);
            JSONObject jsonbody = new JSONObject(jsonString);
            ParseItems(items , jsonbody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }catch (IOException ioe) {
            Log.e(TAG , "Failed to fetch items" , ioe);
        }
        return items;
    }

    private void ParseItems(List<GalleryItem> items , JSONObject jsonbody) throws IOException ,JSONException{
        JSONObject photosJsonObject = jsonbody.getJSONObject("photos");
        JSONArray photoJsonArray  = photosJsonObject.getJSONArray("photo");
        for (int i =0; i<= photoJsonArray.length();i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }
}
