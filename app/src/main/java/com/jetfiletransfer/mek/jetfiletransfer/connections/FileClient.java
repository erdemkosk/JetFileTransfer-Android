package com.jetfiletransfer.mek.jetfiletransfer.connections;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jetfiletransfer.mek.jetfiletransfer.ClientOrServerActivity;
import com.jetfiletransfer.mek.jetfiletransfer.IntroActivity;
import com.jetfiletransfer.mek.jetfiletransfer.MainActivity;
import com.jetfiletransfer.mek.jetfiletransfer.R;
import com.jetfiletransfer.mek.jetfiletransfer.controllers.FileClientController;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.Helpers;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.SharedPreferencesHelper;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppSettings;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusEnum;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppStatusModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModelEnum;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileStatusChanged;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileTransferModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.ServerInformation;
import com.jetfiletransfer.mek.jetfiletransfer.models.TcpConnectionStatus;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileClient extends Service {
    /** indicates how to behave if the service is killed */

    protected Socket socket = null;
    protected Thread thread;
    protected Timer timerPost;
    protected Gson gson = new Gson();
    boolean isRunning = true;
    static long maxTotalSpace = 10*1024*1024; //mb
    private boolean isPro=false;

    protected String folderPath;
    protected long deltaTimePost = 0;
    protected BlockingQueue<FileItemModel> fileQueue;
    protected BufferedInputStream bis;
    protected DataInputStream dis;
    protected BufferedOutputStream bos;
    protected DataOutputStream dos;
    private ServerInformation information;
    protected long totalTransferedBytesInoneSecondSend = 0;
    protected long totalTransferedBytesInoneSecondGet = 0;
    protected long deltaTimeGet = 0;
    protected Timer timerGet;
    private int errorCallbackNumber=0;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public FileClient getService() {
            // Return this instance of LocalService so clients can call public methods
            return FileClient.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void connectToServer() throws IOException {
        generateFolderPath();
        start();

    }
    public BlockingQueue<FileItemModel> getFileQueue() {
        return fileQueue;
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(runnable);
            thread.start();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fileQueue = new LinkedBlockingDeque<FileItemModel>();
        Bundle bundle=null;
        if(intent!=null){
            bundle = intent.getExtras();

        }

        if (bundle != null)
        {
            String informationString = intent.getStringExtra("information");
            information = gson.fromJson(informationString, ServerInformation.class);
            try {

                connectToServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);

    }


    private Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            // Find the server using UDP broadcast
            try {
                final String serverIp= information.getServerIP().substring(1, information.getServerIP().length());
                socket = new Socket(serverIp, information.getServerPort());
                bis = new BufferedInputStream(socket.getInputStream());
                dis = new DataInputStream(bis);
                bos = new BufferedOutputStream(socket.getOutputStream());
                dos = new DataOutputStream(bos);

                EventBus.getDefault().post(new TcpConnectionStatus(true,null,information));
            }  catch (Exception e) {
                e.printStackTrace();
                EventBus.getDefault().post(new TcpConnectionStatus(false,e));
            }
            try {
                sendFileToConnection();
            } catch (IOException ex) {
                Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            while (isRunning) {
                waitFilesFromConnection();
            }



        } };


    @Override
    public void onCreate() {
       isPro = checkIsProVersion();

    }
    public void stopBroadcast(){
        isRunning = false;
    }
    @Override
    public void onDestroy() {
        stopBroadcast();
        try {
            if(socket!=null){
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        stopSelf();
    }



    public ArrayList<FileItemModel> generateItemModels(List<File> fileList) throws IOException {

        ArrayList<FileItemModel> models = new ArrayList<>();
        ArrayList<File> folders = new ArrayList<>();

        // notifyThreadNeedTimeToFectchRequested(); // threde zaman lazım diyorum

        for (File file : fileList) {
            if (file.isDirectory() == true) { // Eleminate folders
                folders.add(file);
                continue;
            }
            String name = file.getName();
            if( isUplimitSendingFile(file)==false){

            FileItemModel model = new FileItemModel(name, 0, "00:00", "Waiting", file);
            model.setFileItemModelEnum(FileItemModelEnum.Send);
            EventBus.getDefault().post(model);//Send Event
            models.add(model);


        for (File folder : folders) {

            List<File> folderInside = Helpers.FileAndFolderHelper.listFilesAndFilesSubDirectories(folder); // folderin içinde ki tüm filelear
            for (File files : folderInside) {

                String folderNames = Helpers.FileAndFolderHelper.listDirectory(files, folder.getParentFile().getName());
                FileItemModel modelFile = new FileItemModel(files.getName(), 0, "00:00", "Waiting", files, folderNames);
                //notifyPostObserversNewFileRequested(model);
                modelFile.setFileItemModelEnum(FileItemModelEnum.Send);
                models.add(modelFile);
            }

        } }

        }
        //notifyThreadCompleteToFectchRequested(); // threde işi bitti
        return models;
    }
    public void sendFileToConnection() throws IOException {
        new Thread() {
            public void run() {
                while (isRunning == true) {
                    try {
                        FileItemModel model = fileQueue.take();

                        if (model == null) {
                            continue;
                        }

                        try {
                            //notifyObserversFileProgressed(fileQueue.size());
                            String name = model.getFileName();
                            long length = model.getFile().length();
                            FileTransferModel transferModel;
                            if (model.getFolderList() != null) { // yaratılması gereken klasörleri varsa
                                transferModel = new FileTransferModel(name, length, model.getFolderList());
                            } else {
                                transferModel = new FileTransferModel(name, length);
                            }

                            String readedJson = gson.toJson(transferModel);
                            dos.writeUTF(readedJson);
                            dos.flush();
                            runTimerPost(model);
                            //notifyNewFileRequested(model);


                            FileInputStream fis = new FileInputStream(model.getFile());

                            //buffer for file writing, to declare inside or outside loop?
                            int n = 0;
                            double pertange = 0;
                            double totalRead = 0;

                            byte[] buf = new byte[4096];

                            while ((n = fis.read(buf)) != -1) {
                                bos.write(buf, 0, n);
                                bos.flush();
                                totalRead += n;
                                totalTransferedBytesInoneSecondSend += n;
                                pertange = (totalRead * 100) / length;
                                model.setPercentageOfLoadedFile(pertange);

                                //notifyGetObserversPercentageChanged(fileItem, pertangeFormated);
                                EventBus.getDefault().post(new FileStatusChanged(FileItemModelEnum.Send));//Send Event

                            }
                            stopTimerPost();
                            model.setTransferSpeed("Completed!");

                        } catch (IOException ex) {


                        }
                    } catch (Exception ex) {




                    }
                }
            }
        }.start();

    }

    protected void runTimerGet(final FileItemModel model) {
        timerGet = new Timer();

        timerGet.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                deltaTimeGet++;

                long transferSpeed = totalTransferedBytesInoneSecondGet;
                totalTransferedBytesInoneSecondGet = 0;

                model.setTime(Helpers.NumericHelper.convertSecondsToHMmSs(deltaTimeGet));
                model.setTransferSpeed(FileUtils.byteCountToDisplaySize(transferSpeed).toLowerCase() + "/s");
            }
        }, 0, 1000);

    }
    protected void stopTimerGet() {

        deltaTimeGet = 0;
        timerGet.cancel();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void waitFilesFromConnection() {

        try {

            String json = dis.readUTF();
//            notifyGetObserversNewFileRequested(fileItem); //notify

            FileTransferModel transferModel = gson.fromJson(json, FileTransferModel.class);

            FileItemModel fileItem = new FileItemModel(transferModel.getFileName(), 0, "00:00", "");
            fileItem.setFileItemModelEnum(FileItemModelEnum.Get);
            EventBus.getDefault().post(fileItem);//Send Event
            notifyThis("New File From Server!", fileItem.getFileName());



            File savedfile = null;
            if (transferModel.getFolders() != null) {
                Path path = Paths.get(folderPath + transferModel.getFolders());

                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    System.err.println("Cannot create directories - " + e);
                }

                System.out.println(transferModel.getFolders());
                savedfile = new File(folderPath + transferModel.getFolders() + "/" + transferModel.getFileName());

            } else {
                savedfile = new File(folderPath + "/" + transferModel.getFileName());
            }
            if (savedfile != null) {
                FileOutputStream fos=null;
                BufferedOutputStream bos=null;
                try{
                    fos = new FileOutputStream(savedfile);
                    bos = new BufferedOutputStream(fos);
                }catch (Exception ex){

                }

                double pertange = 0;
                double totalRead = 0;

                runTimerGet(fileItem);

                int n = 0;
                byte[] buf = new byte[4096];
                long fileSize = transferModel.getFileSize();
                while (fileSize > 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                    fos.write(buf, 0, n);
                    fileSize -= n;
                    totalRead += n;
                    totalTransferedBytesInoneSecondGet += n;
                    pertange = (totalRead * 100) / transferModel.getFileSize();

                    int pertangeFormated = Integer.parseInt(new DecimalFormat("###").format(pertange));
                    fileItem.setPercentageOfLoadedFile(pertangeFormated);

                    //notifyGetObserversPercentageChanged(fileItem, pertangeFormated);
                    EventBus.getDefault().post(new FileStatusChanged(FileItemModelEnum.Get));//Send Event
                }
                fileItem.setTransferSpeed("Completed!");
                stopTimerGet();
                totalTransferedBytesInoneSecondGet = 0;
                bos.close();
                MediaScannerConnection.scanFile(getBaseContext(),new String[] { folderPath + "/" + transferModel.getFileName() }, null,new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
            }

//dis.close();}
        } catch (Exception ex) {

            if (errorCallbackNumber==0){
                connectionClosedByOtherSide();
            }
            errorCallbackNumber++;

        }
    }
    private void generateFolderPath(){
        if (folderPath==null){
            try{

                File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "JetFileTransfer");
                if (!f.exists()) {
                    f.mkdirs();
                }
                folderPath = f.getPath();
            }catch (Exception ex){

            }
        }
    }

    private void connectionClosedByOtherSide(){
        EventBus.getDefault().post(new AppStatusModel(AppStatusEnum.closedbyOtherSideClient));
    }
    protected void runTimerPost(final FileItemModel model) {
        timerPost = new Timer();

        timerPost.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                deltaTimePost++;

                long transferSpeed = totalTransferedBytesInoneSecondSend;
                totalTransferedBytesInoneSecondSend = 0;

                model.setTime(Helpers.NumericHelper.convertSecondsToHMmSs(deltaTimePost));
                model.setTransferSpeed(FileUtils.byteCountToDisplaySize(transferSpeed).toLowerCase(Locale.ENGLISH) + "/s");
            }
        }, 0, 1000);

    }

    protected void stopTimerPost() {

        deltaTimePost = 0;
        timerPost.cancel();
    }
    public void notifyThis(String title, String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isShowNotification = prefs.getBoolean("notifications_new_message",true);
        if(isShowNotification==true){


            NotificationManager mNotificationManager;

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getBaseContext().getApplicationContext(), "notify_001");
            Intent ii = new Intent(this,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, ii,PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.setBigContentTitle(title);
            bigText.setSummaryText(message);

            mBuilder.setContentIntent(pendingIntent);

            mBuilder.setSmallIcon(R.drawable.appuncolor);


            mBuilder.setContentTitle(title);
            mBuilder.setContentText(message);
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setStyle(bigText);
            mBuilder.setAutoCancel(true);
            mNotificationManager =
                    (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("notify_001",
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(channel);
            }

            mNotificationManager.notify(0, mBuilder.build());
        }

    }
    private boolean checkIsProVersion(){
        SharedPreferencesHelper secureSharedHelper = new SharedPreferencesHelper(this,true);
        return secureSharedHelper.checkAppStatus();
    }
    private boolean isUplimitSendingFile(final File file){
        if(isPro==false){


       long totalSpace =  file.length(); //byte
       Log.d("total",Long.toString(totalSpace));
       if(totalSpace>maxTotalSpace){

           new Handler(Looper.getMainLooper()).post(new Runnable() {
               public void run() {
                   Toast.makeText(FileClient.this,file.getName() + " is bigger than 20mb. Please buy pro version!",
                           Toast.LENGTH_SHORT).show();
               }
           });

           return true;

       }
       else{
           return false;
       }
        }
        else{
            return false;
        }

    }
}


