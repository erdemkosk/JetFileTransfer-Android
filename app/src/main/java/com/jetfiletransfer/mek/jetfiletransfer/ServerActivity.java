package com.jetfiletransfer.mek.jetfiletransfer;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.jetfiletransfer.mek.jetfiletransfer.connections.BroadcastServer;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;
import com.jetfiletransfer.mek.jetfiletransfer.controllers.FileServerController;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.ServiceHelper;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.IActivityController;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusEnum;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class ServerActivity extends AppCompatActivity implements IActivityController {
    LinearLayout rootLayout;
    FileServerController serverController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        rootLayout = findViewById(R.id.rootLayout);



        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted, open the camera
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            Snackbar.make(rootLayout, "This app needs this permission!", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        openServices();
        serverController = new FileServerController(getApplicationContext());
        serverController.run();
    }




    private  void startBroadcastServer(){
        startService(new Intent(ServerActivity.this, BroadcastServer.class));
    }
    private  void stopBroadcastServer(){
        stopService(new Intent(ServerActivity.this, BroadcastServer.class));
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
    public void onMessageEvent(final AppStatusModel model) {
        if (model.getAppStatus() == AppStatusEnum.connected){
            stopBroadcastServer();
            Snackbar.make(rootLayout, "Client Connected @" + model.getIpAdress(), Snackbar.LENGTH_LONG).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    Intent myIntent = new Intent(ServerActivity.this, MainActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Bundle b = new Bundle();
                    b.putInt("key", 0); //0 Server 1 Client
                    myIntent.putExtras(b); //Put your id to your next Intent
                    startActivity(myIntent);
                }
            }, 800);

        }
    };

    @Override
    public void openServices() {
        if (ServiceHelper.isServiceRunning(ServerActivity.this,".connections.BroadcastServer")!=true){
            startBroadcastServer();
        }
    }

    @Override
    public void closeServices() {
        stopBroadcastServer();
    }
    @Override
    public void onBackPressed() {
        MaterialStyledDialog dialog= new MaterialStyledDialog.Builder(this)
                .setTitle("Exit?")
                .setDescription("Are you sure you want to back?")
                .setHeaderColor(R.color.colorPrimary)
                .setIcon(R.drawable.app)
                .setPositiveText("Yes!")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        stopFileServerService();
                        Intent myIntent = new Intent(ServerActivity.this, ClientOrServerActivity.class);
                        startActivity(myIntent);

                    }
                })
                .setNegativeText("No!")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .withIconAnimation(true).show();
    }
    private void stopFileServerService(){
        stopService(new Intent(ServerActivity.this, FileServer.class));

    }
}
