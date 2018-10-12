package com.jetfiletransfer.mek.jetfiletransfer;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jetfiletransfer.mek.jetfiletransfer.connections.BroadcastClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;
import com.jetfiletransfer.mek.jetfiletransfer.controllers.FileClientController;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.ServiceHelper;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.IActivityController;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusEnum;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.ServerInformation;
import com.jetfiletransfer.mek.jetfiletransfer.models.TcpConnectionStatus;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ClientActivity extends AppCompatActivity implements IActivityController {
    MaterialSpinner spinner;
    ArrayList<ServerInformation> allServerInformations = new ArrayList<>();
    ServerInformation selectedInformation;
    FileClientController clientController;
    LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        openServices();

        rootLayout = findViewById(R.id.rootLayout);

        spinner = (MaterialSpinner) findViewById(R.id.clientSpinner);

        Button connectButton= (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBroadcastClient();
                //Snackbar.make(v, "Servis Durdu", Snackbar.LENGTH_LONG).show();
                if(allServerInformations!=null && allServerInformations.size()>0 ){
                    selectedInformation = allServerInformations.get(spinner.getSelectedIndex());
                    if(selectedInformation!=null){
                        clientController = new FileClientController(getApplicationContext(),selectedInformation);
                        startFileClientService();
                    }
                    else{
                        Snackbar.make(rootLayout, "At least you must select an item!" , Snackbar.LENGTH_LONG).show();
                    }
                }
                else{
                    Snackbar.make(rootLayout, "At least you must select an item!" , Snackbar.LENGTH_LONG).show();
                }



            }
        });
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



    @Subscribe()
    public void onMessageEvent(ServerInformation information) {
        if (allServerInformations.contains(information)==false){
            allServerInformations.add(information);
            spinner.setItems(allServerInformations);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TcpConnectionStatus status) {
        if (status.isConnected()!=true){
            Snackbar.make(rootLayout, "Connection Error!", Snackbar.LENGTH_LONG).show();
            stopFileClientService();
        }
        else{
            Snackbar.make(rootLayout, "Connected at" + status.getHostInformation().getServerHostName() + "@" + status.getHostInformation().getServerIP() , Snackbar.LENGTH_LONG).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent myIntent = new Intent(ClientActivity.this, MainActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Bundle b = new Bundle();
                    b.putInt("key", 1); //0 Server 1 Client
                    myIntent.putExtras(b); //Put your id to your next Intent
                    startActivity(myIntent);
                }
            }, 800);   //5 seconds

        }
    };
    @Override
    protected void onDestroy() {
        closeServices();
        super.onDestroy();
    }

    private void startFileClientService(){
        clientController.run();
    }
    private void stopFileClientService(){
        stopService(new Intent(ClientActivity.this, FileClient.class));

    }
    private  void startBroadcastClient(){
        startService(new Intent(ClientActivity.this, BroadcastClient.class));
    }
    private  void stopBroadcastClient(){
        stopService(new Intent(ClientActivity.this, BroadcastClient.class));
    }


    @Override
    public void openServices() {
        if (ServiceHelper.isServiceRunning(ClientActivity.this,".connections.BroadcastClient")!=true){
            startBroadcastClient();
        }
        else{
            stopBroadcastClient();
        }
    }

    @Override
    public void closeServices() {
        stopFileClientService();
        stopBroadcastClient();
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

                        stopFileClientService();
                        Intent myIntent = new Intent(ClientActivity.this, ClientOrServerActivity.class);
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



}
