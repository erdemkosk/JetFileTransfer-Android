package com.jetfiletransfer.mek.jetfiletransfer.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.jetfiletransfer.mek.jetfiletransfer.ClientOrServerActivity;
import com.jetfiletransfer.mek.jetfiletransfer.connections.BroadcastClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.BroadcastServer;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.IController;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.ITcpIpObserver;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppSettings;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.ServerInformation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileClientController implements IController, ITcpIpObserver {

    Context context;
    private BlockingQueue queue = new LinkedBlockingDeque<FileItemModel>();
    ServerInformation information;
    Gson gson = new Gson();

    public FileClientController(Context context,ServerInformation information) {
        this.context = context;
        this.information=information;


    }

    protected void readAndLoadSettings() {

    }
    public void run() {
        beforeStart();
        doStart();
        afterStart();
    }

    @Override
    public void beforeStart() {
        readAndLoadSettings();
        //showMainWindow(TcpIpType.FileClient);
    }


    @Override
    public void doStart() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "JetFileTransfer");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
        }

        Intent serviceIntent = new Intent(context,FileClient.class);
        String appSettingString = gson.toJson(new AppSettings(4444,mediaStorageDir.getPath(),8888,false));
        String informationString = gson.toJson(information);
        Bundle bundle = new Bundle();
        bundle.putString("appSettings", appSettingString);
        bundle.putString("information",informationString);
        serviceIntent.putExtras(bundle);
        context.startService(serviceIntent);
        

    }

    @Override
    public void afterStart() {

    }


    public void connectServerRequested(ServerInformation inform) {
        try {
            //fileClient.connectToServer(inform);

        } catch (Exception ex) {

        }

    }





    public void newPostFileRequested(FileItemModel model) {

    }

    @Override
    public void percentageChangedPostRequested(FileItemModel file, double percentage) {


    }

    @Override
    public void newGetFileRequested(FileItemModel model) {

    }

    @Override
    public void percentageChangedGetRequested(FileItemModel file, double percentage) {

    }


    @Override
    public void connectionFailedRequested() {


    }

    @Override
    public void clientConnectedRequested(String destinationIP) {

    }

    @Override
    public void serverConnectedRequested(String destinationIP) {

    }

    @Override
    public void fileProgressedRequested(int count) {

    }

    @Override
    public void threadNeedTimeToFectch() {


    }

    @Override
    public void threadCompleteToFectch() {



    }

    @Override
    public void sendingStatusChange(boolean isReady) {

    }

    @Override
    public void disconnect() {

    }
}

