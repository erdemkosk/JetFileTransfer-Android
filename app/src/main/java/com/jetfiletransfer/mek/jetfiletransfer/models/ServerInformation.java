package com.jetfiletransfer.mek.jetfiletransfer.models;

import java.io.Serializable;
import java.util.Objects;

public class ServerInformation implements Serializable{
    private String serverIP;
    private int serverPort;
    private String serverHostName;

    public String getServerHostName() {
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public ServerInformation(String serverIP, int serverPort, String serverHostName) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.serverHostName = serverHostName;
    }

    public ServerInformation(String serverHostName, int serverPort) {

        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
    }

    @Override
    public String toString() {
        return serverHostName + serverIP + ":" + serverPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerInformation)) return false;
        ServerInformation that = (ServerInformation) o;
        return getServerPort() == that.getServerPort() &&
                Objects.equals(getServerIP(), that.getServerIP()) &&
                Objects.equals(getServerHostName(), that.getServerHostName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getServerIP(), getServerPort(), getServerHostName());
    }
}

