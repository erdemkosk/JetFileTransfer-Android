package com.jetfiletransfer.mek.jetfiletransfer.connections;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Debug;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jetfiletransfer.mek.jetfiletransfer.R;
import com.jetfiletransfer.mek.jetfiletransfer.models.ServerInformation;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class BroadcastClient extends Service {
    /** indicates how to behave if the service is killed */

    DatagramSocket socket;
    protected Thread thread;
    protected String threadName;
    protected int portNumber;
    protected Gson gson = new Gson();
    boolean isRunning = true;


    public void start() {

        if (thread == null) {
            thread = new Thread(runnable);
            thread.start();

        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Find the server using UDP broadcast
            while (isRunning == true) {

                try {

                    //Open a random port to send the package
                    socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
                    //Try the 255.255.255.255 first
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                        socket.send(sendPacket);
                    } catch (Exception e) {
                    }
                    // Broadcast the message over all the network interfaces
                    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                            continue; // Don't want to broadcast to the loopback interface
                        }
                        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                            InetAddress broadcast = interfaceAddress.getBroadcast();
                            if (broadcast == null) {
                                continue;
                            }
                            // Send the broadcast package!
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                                socket.send(sendPacket);
                            } catch (Exception e) {
                            }
                        }
                    }
                    socket.setSoTimeout(10000);
                    //Wait for a response
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(receivePacket);
                    //We have a response
                    //Check if the message is correct
                    String message = new String(receivePacket.getData()).trim();


                    if (message.contains("DISCOVER_FUIFSERVER_RESPONSE")) {
                        System.out.println(message);
                        String[] parts = message.split(">");

                        ServerInformation information = gson.fromJson(parts[1], ServerInformation.class);

                        information.setServerIP(receivePacket.getAddress().toString());
                        EventBus.getDefault().post(information); // Send Event

                        Log.d("Yeep",information.getServerIP());
                        //isRunning = false;
                    }
                    //Close the port!
                    socket.close();
                } catch (IOException ex) {

                }
            }
        } };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        start();
    }
    public void stopBroadcast(){
        isRunning = false;
    }
    @Override
    public void onDestroy() {
        stopBroadcast();

    }

}


