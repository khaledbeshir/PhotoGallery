package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {


    public static Intent NewIntent(Context context){
        return new Intent(context ,PhotoGalleryActivity.class);
    }

    @Override
    public Fragment CreateFragment() {
        return PhotoGalleryFragment.NewInstance();
    }


}
