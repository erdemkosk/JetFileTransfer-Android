package com.jetfiletransfer.mek.jetfiletransfer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusEnum;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModelEnum;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileStatusChanged;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    DownloadFragment downloadFragment = new DownloadFragment();
    UploadFragment uploadFragment = new UploadFragment();
    ConstraintLayout container;
    int clientOrServer=-1; //0 Server 1 Client
    private NotificationCompat.Builder notification_builder;
    private NotificationManagerCompat notification_manager;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final FileItemModel model) {
        if(model.getFileItemModelEnum()==FileItemModelEnum.Get){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    downloadFragment.setItemToListView(model);


                }
            });
    }
    if(model.getFileItemModelEnum()==FileItemModelEnum.Send){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    uploadFragment.setItemToListView(model);
                }
            });
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final FileStatusChanged model) {
        if(model.getFileItemModelEnum()==FileItemModelEnum.Get){
    new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
            downloadFragment.NotifyChanged(); }
    });
    }
    if(model.getFileItemModelEnum()==FileItemModelEnum.Send){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    uploadFragment.NotifyChanged(); }
            });
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transection = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_upload:
                    transection.replace(R.id.content,uploadFragment).commit();
                    return true;
                case R.id.navigation_download:
                    transection.replace(R.id.content,downloadFragment).commit();

                    return true;
                case R.id.navigation_folder:

                    openDownloads(MainActivity.this);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle b = intent.getExtras();
        if(b!=null){
            ArrayList<String> urlStrings = b.getStringArrayList("handledValue");
            uploadFragment.externalFilesRequested(urlStrings);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Bundle b = getIntent().getExtras();
        clientOrServer = -1; // or other values
        if(b != null){


            clientOrServer = b.getInt("key");
        }


        getSupportActionBar().setTitle("Jet File Transfer");
        container = findViewById(R.id.container);



        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transection = fragmentManager.beginTransaction();
        transection.replace(R.id.content,uploadFragment).commit();
        if(clientOrServer!=-1){
            uploadFragment.selectServiceType(clientOrServer);
        }
    }

    public static void openDownloads(@NonNull Activity activity) {
        if (isSamsung()) {
            Intent intent = activity.getPackageManager()
                    .getLaunchIntentForPackage("com.sec.android.app.myfiles");
            intent.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
            intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                    getDownloadsFile().getPath());
            activity.startActivity(intent);
        }
        else {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                    + "/JetFileTransfer/");
            intent.setDataAndType(uri, "*/*");
            activity.startActivity(Intent.createChooser(intent, "Open folder"));
        }
    }

    public static boolean isSamsung() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) return manufacturer.toLowerCase().equals("samsung");
        return false;
    }

    public static File getDownloadsFile() {

        return  new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "JetFileTransfer");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventFromConnection(AppStatusModel model) {
        // Karşı taraf bağlantıyı kopartmıs
        if (model.getAppStatus() == AppStatusEnum.closedbyOtherSideClient){
            Snackbar.make(container, "Connection closed by other side !" , Snackbar.LENGTH_LONG).show();
            stopFileClientService();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent myIntent = new Intent(MainActivity.this, ClientActivity.class);

                    startActivity(myIntent);
                }
            }, 1000);
        }
        else if (model.getAppStatus() == AppStatusEnum.closedbyOtherSideServer){
            Snackbar.make(container, "Connection closed by other side !" , Snackbar.LENGTH_LONG).show();
            stopFileServerService();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent myIntent = new Intent(MainActivity.this, ServerActivity.class);
                    startActivity(myIntent);
                }
            }, 1000);
        }


    };

    private void stopFileClientService(){
        stopService(new Intent(MainActivity.this, FileClient.class));

    }
    private void stopFileServerService(){
        stopService(new Intent(MainActivity.this, FileServer.class));

    }
    @Override
    public void onBackPressed() {
        MaterialStyledDialog dialog= new MaterialStyledDialog.Builder(this)
                .setTitle("Exit?")
                .setDescription("Are you sure you want to disconnect?\n" +
                        "If you disconnect, the app will stop receiving and sending files.")
                .setHeaderColor(R.color.colorPrimary)
                .setIcon(R.drawable.app)
                .setPositiveText("Yes!")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        stopFileClientService();
                        stopFileServerService();
                        Intent myIntent = new Intent(MainActivity.this, ClientOrServerActivity.class);
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
