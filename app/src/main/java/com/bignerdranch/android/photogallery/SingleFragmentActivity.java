package com.bignerdranch.android.photogallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mohamed Amr on 5/8/2019.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    public abstract Fragment CreateFragment ();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = CreateFragment();
            fm.beginTransaction().add(R.id.fragment_container , fragment).commit();
        }
    }
}
