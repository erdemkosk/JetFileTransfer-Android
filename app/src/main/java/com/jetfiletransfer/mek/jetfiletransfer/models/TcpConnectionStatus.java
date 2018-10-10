package com.jetfiletransfer.mek.jetfiletransfer.models;

public class TcpConnectionStatus {
    private boolean isConnected;
    private Exception exception;
    private ServerInformation hostInformation;

    public TcpConnectionStatus(boolean isConnected, Exception exception) {
        this.isConnected = isConnected;
        this.exception = exception;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public TcpConnectionStatus(boolean isConnected, Exception exception, ServerInformation hostInformation) {
        this.isConnected = isConnected;
        this.exception = exception;
        this.hostInformation = hostInformation;
    }

    public ServerInformation getHostInformation() {
        return hostInformation;
    }

    public void setHostInformation(ServerInformation hostInformation) {
        this.hostInformation = hostInformation;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
