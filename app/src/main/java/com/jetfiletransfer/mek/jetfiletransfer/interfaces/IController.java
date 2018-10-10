package com.jetfiletransfer.mek.jetfiletransfer.interfaces;

public interface IController extends IControllerTemplate{
    public void run();

   // public void hookMainAppObserver(IApp client);

   // public void unHookMainAppObserver(IApp client);

    public void disconnect();
}
