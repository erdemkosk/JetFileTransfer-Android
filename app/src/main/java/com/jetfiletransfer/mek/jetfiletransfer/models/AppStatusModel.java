package com.jetfiletransfer.mek.jetfiletransfer.models;

public class AppStatusModel {
    private  AppStatusEnum appStatus;
    private String ipAdress;

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public AppStatusModel(AppStatusEnum appStatus, String ipAdress) {
        this.appStatus = appStatus;
        this.ipAdress = ipAdress;
    }
    public AppStatusModel(AppStatusEnum appStatus) {
        this.appStatus = appStatus;

    }

    public AppStatusEnum getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(AppStatusEnum appStatus) {
        this.appStatus = appStatus;
    }
}
