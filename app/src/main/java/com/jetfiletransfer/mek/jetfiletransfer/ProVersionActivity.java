package com.jetfiletransfer.mek.jetfiletransfer;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.PurchasesManager;
import com.jetfiletransfer.mek.jetfiletransfer.models.PurchasesModel;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ProVersionActivity extends AppCompatActivity {


    PurchasesManager manager ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pro_version);
        manager = new PurchasesManager(this);
        //billingProcess();

        getSupportActionBar().setTitle("Support Us!");


        findViewById(R.id.dummy_button).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                manager.buyProVersion("buy_pro");
            }
        });
    }



}
