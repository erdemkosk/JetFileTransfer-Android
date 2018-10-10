package com.jetfiletransfer.mek.jetfiletransfer.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.IController;
import com.jetfiletransfer.mek.jetfiletransfer.interfaces.ITcpIpObserver;
import com.jetfiletransfer.mek.jetfiletransfer.models.AppSettings;

public class FileServerController implements IController {
    Context context;

    public FileServerController(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        beforeStart();
        doStart();
        afterStart();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void beforeStart() {

    }

    @Override
    public void doStart() {

        Intent serviceIntent = new Intent(context,FileServer.class);
        context.stopService(serviceIntent);
        context.startService(serviceIntent);
    }

    @Override
    public void afterStart() {

    }
}
