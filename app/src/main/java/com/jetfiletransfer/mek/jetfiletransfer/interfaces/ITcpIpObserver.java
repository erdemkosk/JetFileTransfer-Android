package com.jetfiletransfer.mek.jetfiletransfer.interfaces;

import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;

public interface ITcpIpObserver {
    public void newPostFileRequested(FileItemModel model);

    public void percentageChangedPostRequested(FileItemModel file, double percentage);

    public void newGetFileRequested(FileItemModel model);

    public void percentageChangedGetRequested(FileItemModel file, double percentage);

    public void clientConnectedRequested(String destinationIP);

    public void serverConnectedRequested(String destinationIP);

    public void connectionFailedRequested();

    public void fileProgressedRequested(int count); // File Count

    public void threadNeedTimeToFectch();

    public void threadCompleteToFectch();

    public void sendingStatusChange(boolean isReady);
}
