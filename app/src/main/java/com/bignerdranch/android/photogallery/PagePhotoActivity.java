package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

/**
 * Created by Mohamed Amr on 6/4/2019.
 */

public class PagePhotoActivity extends SingleFragmentActivity {

    public static Intent newIntent (Context context, Uri uri){
        Intent intent = new Intent(context , PagePhotoActivity.class);
        intent.setData(uri);
        return intent;
    }
    @Override
    public Fragment CreateFragment() {
        return PagePhotoFragment.newInstance(getIntent().getData());
    }

    @Override
    public void onBackPressed() {
        if (PagePhotoFragment.mWebView.canGoBack()){
            PagePhotoFragment.mWebView.goBack();
        }else {
            super.onBackPressed();
        }
    }
}
