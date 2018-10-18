package com.jetfiletransfer.mek.jetfiletransfer.connections;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jetfiletransfer.mek.jetfiletransfer.models.ServerInformation;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class BroadcastServer extends Service {
    /** indicates how to behave if the service is killed */

    MulticastSocket socket;
    protected Thread thread;
    protected String threadName;
    protected int portNumber;
    protected Gson gson = new Gson();
    boolean isRunning = true;

    WifiManager wifi;
    WifiManager.MulticastLock mLock ;




    public void start() {

        if (thread == null) {
            thread = new Thread(runnable);
            thread.start();

        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            releaseWifiLock();

             mLock.acquire();
            // Find the server using UDP broadcast
            while (isRunning == true) {

                try {
                    //Keep a socket open to listen to all the UDP trafic that is destined for this port
                    try {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BroadcastServer.this);
                        String savedString = prefs.getString("udp_port_number","8888");
                        int portNumber = Integer.parseInt(savedString);
                        socket = new MulticastSocket(portNumber);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.setBroadcast(true);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                    while (true) {

                        //Receive a packet
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

                        socket.receive(packet);

                        //Packet received
                        //See if the packet holds the right command (message)
                        String message = new String(packet.getData()).trim();
                        if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                            String deviceName = android.os.Build.MODEL; // returns model name
                            String deviceManufacturer = android.os.Build.MANUFACTURER; // returns manufactu
                            String hostName = capitalize(deviceManufacturer) +" " +  deviceName + "(Phone)";
                            //Test verileri okunacak
                            ServerInformation serverInfo = new ServerInformation(hostName, 4444);

                            String gsonSettingString = gson.toJson(serverInfo, ServerInformation.class);

                            String discoverServerMessage = "DISCOVER_FUIFSERVER_RESPONSE>" + gsonSettingString;
                            byte[] sendData = discoverServerMessage.getBytes();

                            //Send a response
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());

                            socket.send(sendPacket);
                            releaseWifiLock();
                            //isRunning = false;
                            //cdisRunning = false;

                        }
                    //Close the port

                    socket.close();
                }
            } catch (UnknownHostException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        wifi = (WifiManager)BroadcastServer.this.getSystemService(Context.WIFI_SERVICE);
        mLock = wifi.createMulticastLock("lock");
        start();

    }
    public void stopBroadcast(){
        releaseWifiLock();
        isRunning = false;
    }
    @Override
    public void onDestroy() {
        stopBroadcast();
        stopSelf();

    }
    private void releaseWifiLock(){
        try {
            if (mLock.isHeld()) {
                mLock.release();
            }
        } catch (Exception e) {
            // just to make sure if the PowerManager crashes while acquiring a wake lock
           Log.d("s","s");
        }
    }
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }
    private  String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }



}


