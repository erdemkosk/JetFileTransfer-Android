package com.jetfiletransfer.mek.jetfiletransfer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.Visibility;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.jetfiletransfer.mek.jetfiletransfer.connections.BroadcastClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.BroadcastServer;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.PurchasesManager;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.ServiceHelper;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.SharedPreferencesHelper;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.IActivityController;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.IController;
import com.jetfiletransfer.mek.jetfiletransfer.models.PurchasesModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.TcpConnectionStatus;
import com.securepreferences.SecurePreferences;
import com.skyfishjy.library.RippleBackground;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ClientOrServerActivity extends AppCompatActivity implements IActivityController {
    ImageView server,client;
    SharedPreferencesHelper sharedHelper = new SharedPreferencesHelper(this);
    boolean usingPro=false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_or_server);
        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
        rippleBackground.startRippleAnimation();


        Intent intent = getIntent();
        boolean isPro = intent.getBooleanExtra("key",false); //if it's a string you stored.
        if(isPro==true){
            //buy_pro_version.setVisibility(View.GONE);
            usingPro=true;
        }

        server =  findViewById(R.id.server_image);
        client = findViewById(R.id.client_image);

        //Fresh Start
        closeServices();



        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);

                v.startAnimation(animFadein);
                Intent intent = new Intent(ClientOrServerActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });
        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);

                v.startAnimation(animFadein);
                Intent intent = new Intent(ClientOrServerActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });


        checkandShowHelpingView();

    }
    private void checkandShowHelpingView(){
        if(sharedHelper.getHelperViewPref()==true){
            TapTargetView.showFor(this,                 // `this` is an Activity
                    TapTarget.forView(findViewById(R.id.app_image), "This is Jet File Transfer!", "An application that allows you to transfer files from one device to another, regardless of the operating system, thanks to devices connected to the same network...")
                            // All options below are optional
                            .outerCircleColor(R.color.colorAccent)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.colorPrimary)   // Specify a color for the target circle
                            .titleTextSize(28)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textColor(R.color.white)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)// Specify a custom drawable to draw as the target
                            .targetRadius(60),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);      // This call is optional
                            generateServerHelperView();
                        }
                    });
        }

    }
    private void generateClientHelperView(){
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.client_image), "This is Client!", "Used for connecting to the server...")
                        // All options below are optional
                        .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                        .titleTextSize(28)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textColor(R.color.white)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)// Specify a custom drawable to draw as the target
                        .targetRadius(90),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        if(usingPro==true){
                            sharedHelper.addHelperView();
                        }
                        else{
                            generateProVersionHelperView();
                        }

                    }
                });
    }
    private void generateServerHelperView(){
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.server_image), "This is Server!", "\n" +
                        "A  server allows users to share server information over a network so clients can connect to the server.")
                        // All options below are optional
                        .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                        .titleTextSize(28)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textColor(R.color.white)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)// Specify a custom drawable to draw as the target
                        .targetRadius(90),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        generateClientHelperView();
                    }
                });
    }
    private void generateProVersionHelperView(){
        if(usingPro==false){
            TapTargetView.showFor(this,                 // `this` is an Activity
                    TapTarget.forView(findViewById(R.id.action_pro), "Support Us!",
                            "Upgrade Jet File Transfer to pro version! You will no longer see ads, and your file sending limit will be removed.")
                            // All options below are optional
                            .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                            .titleTextSize(28)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textColor(R.color.white)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)// Specify a custom drawable to draw as the target
                            .targetRadius(90),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);      // This call is optional
                            sharedHelper.addHelperView();
                        }
                    });
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(ClientOrServerActivity.this, SettingsPrefActivity.class));
            return true;
        }
        if (id == R.id.action_pro) {
            // launch settings activity
            if(usingPro==false){
                startActivity(new Intent(ClientOrServerActivity.this, ProVersionActivity.class));
            }
            else{
                startActivity(new Intent(ClientOrServerActivity.this, ProVersionActivity.class));
              //  LinearLayout root = findViewById(R.id.rootLayout);
                //Snackbar snackbar = Snackbar.make(root, "You already have a pro version! Thanks for supporting!", Snackbar.LENGTH_LONG);
                //snackbar.show();
            }

            return true;
        }


        if (id == R.id.send_help) {
            // launch settings activity
            sharedHelper.removeHelperView();
            checkandShowHelpingView();
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void openServices() {

    }

    @Override
    public void closeServices() {
        stopFileServiceService();
        stopFileClientService();
        stopBroadcastClient();
        stopBroadcastServer();

    }
    private void stopFileServiceService(){
        stopService(new Intent(ClientOrServerActivity.this, FileServer.class));
    }
    private void stopFileClientService(){
        stopService(new Intent(ClientOrServerActivity.this, FileClient.class));
    }
    private  void stopBroadcastClient(){
        stopService(new Intent(ClientOrServerActivity.this, BroadcastClient.class));
    }
    private  void stopBroadcastServer(){
        stopService(new Intent(ClientOrServerActivity.this, BroadcastServer.class));
    }
    @Override
    protected void onDestroy() {
        closeServices();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}