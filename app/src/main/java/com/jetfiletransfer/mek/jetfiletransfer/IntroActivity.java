package com.jetfiletransfer.mek.jetfiletransfer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.jetfiletransfer.mek.jetfiletransfer.helpers.PurchasesManager;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.SharedPreferencesHelper;
import com.jetfiletransfer.mek.jetfiletransfer.models.PurchasesModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class IntroActivity extends AppCompatActivity {
    PurchasesManager manager ;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);
        logo = findViewById(R.id.app_logo);
        manager = new PurchasesManager(this);
        animateImage();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PurchasesModel event) {
        checkIsAppProVersion();
    };
    private void checkIsAppProVersion(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                // do something
                SharedPreferencesHelper secureSharedHelper = new SharedPreferencesHelper(IntroActivity.this,true);
                Intent myIntent = new Intent(IntroActivity.this, ClientOrServerActivity.class);
                myIntent.putExtra("key", secureSharedHelper.checkAppStatus()); //Optional parameters
                startActivity(myIntent);
            }
        }, 500);


    }
    private void animateImage(){
        ScaleAnimation fade_in =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fade_in.setDuration(1000);     // animation duration in milliseconds
        fade_in.setFillAfter(true);    // If fillAfter is true, the transformation that this animation performed will persist when it is finished.
        logo.startAnimation(fade_in);
    }



}
