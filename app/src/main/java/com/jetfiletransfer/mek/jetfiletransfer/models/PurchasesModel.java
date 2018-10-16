package com.jetfiletransfer.mek.jetfiletransfer.models;

public  class PurchasesModel {
    boolean isConnectedServer;

    public boolean isConnectedServer() {
        return isConnectedServer;
    }

    public void setConnectedServer(boolean connectedServer) {
        isConnectedServer = connectedServer;
    }

    public PurchasesModel(boolean isConnectedServer) {
        this.isConnectedServer = isConnectedServer;
    }
}
