package com.jetfiletransfer.mek.jetfiletransfer.helpers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Helpers{

 public static class NumericHelper {

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        //long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d", m, s);
    }

}

public static class NetworkHelper {

    public static InetAddress getLocalAddress() {
        try {
            Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
            while (b.hasMoreElements()) {
                for (InterfaceAddress f : b.nextElement().getInterfaceAddresses()) {
                    if (f.getAddress().isSiteLocalAddress()) {
                        return f.getAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}



public static class FileAndFolderHelper {

    public static String listDirectory(File parentDirectory, String folderNameToStop) {
        String folders = "";
        while (parentDirectory != null) {
            parentDirectory = parentDirectory.getParentFile();
            if (parentDirectory.getName().equals(folderNameToStop) != true) {
                if (parentDirectory != null && !parentDirectory.getName().isEmpty()) {

                    folders = "/" + parentDirectory.getName() + folders;
                }
            } else {
                break;
            }

        }
        return folders;
    }

    public static List<File> listFilesAndFilesSubDirectories(File directory) {
        List<File> files = (List<File>) FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        return files;
    }
}




}