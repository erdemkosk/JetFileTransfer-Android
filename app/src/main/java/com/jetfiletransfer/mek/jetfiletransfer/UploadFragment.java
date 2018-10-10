package com.jetfiletransfer.mek.jetfiletransfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;
import com.jetfiletransfer.mek.jetfiletransfer.controllers.FileClientController;
import com.jetfiletransfer.mek.jetfiletransfer.helpers.CustomAdapter;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModelEnum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    FilePickerDialog dialog;
    ArrayList<FileItemModel> dataModels= new ArrayList<>();
    private CustomAdapter adapter;
    FileClient clientService;
    FileServer serverService;
    boolean mBound = false;
    int clientOrServer;
    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        if(clientOrServer==0){
            Intent intent = new Intent(getActivity(), FileServer.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        else{
            Intent intent = new Intent(getActivity(), FileClient.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_upload,
                container, false);

        adapter= new CustomAdapter(dataModels,getActivity());
        setListAdapter(adapter);

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // do something
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.MULTI_MODE;
                properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT;
                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;
                dialog = new FilePickerDialog(getActivity(),properties);
                dialog.setTitle("Select a Folder(s) or File(s)");
                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                       final ArrayList<File> fileArrayList = new ArrayList<>();
                        for (String filePath : files) {
                            fileArrayList.add(new File(filePath));
                        }

                        new Thread() {
                            public void run() {
                                try {
                                    ArrayList<FileItemModel> models;
                                    if(serverService!=null){
                                        models = serverService.generateItemModels(fileArrayList);
                                    }
                                    else{
                                        models = clientService.generateItemModels(fileArrayList);
                                    }

                                    for (FileItemModel file : models) {
                                        if(serverService!=null){
                                           serverService.getFileQueue().put(file);
                                        }
                                        else{ clientService.getFileQueue().put(file);
                                        }

                                        //fileServer.getFileQueue().notifyAll();
                                    }
                                    //fileProgressedRequested(fileClient.getFileQueue().size());
                                } catch (IOException ex) {
                                    Logger.getLogger(FileClientController.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(FileClientController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }.start();
                    }
                });
                dialog.show();

            }
        });
        return view;
    }

    //Add this method to show Dialog when the required permission has been granted to the app.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(dialog!=null)
                    {   //Show dialog if the read permission has been granted.
                        dialog.show();
                    }
                }
                else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(getContext(),"Permission is Required for getting list of files",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void setItemToListView(FileItemModel model) {
        if(model.getFileItemModelEnum()== FileItemModelEnum.Send){
            dataModels.add(0, model);

            if (adapter!=null){
                adapter.notifyDataSetChanged();
            }
        }
    }
    // Handle Item click event
    public void onListItemClick(ListView l, View view, int position, long id){
        ViewGroup viewg=(ViewGroup)view;
        TextView tv=(TextView)viewg.findViewById(R.id.txtitem);
        Toast.makeText(getActivity(), tv.getText().toString(),Toast.LENGTH_LONG).show();

    }


    public void NotifyChanged() {
        if (adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
             FileServer.LocalBinder serverBinder=null;
             FileClient.LocalBinder clientBinder=null;
            if(clientOrServer==0){
                serverBinder = (FileServer.LocalBinder) service;
            }
            else{
                clientBinder = (FileClient.LocalBinder) service;

            }
            if(serverBinder!=null){
                serverService = serverBinder.getService();
            }
            else{
                clientService = clientBinder.getService();
            }


            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void selectServiceType(int clientOrServer) {
        this.clientOrServer = clientOrServer;


    }
    public void externalFilesRequested(ArrayList<String> filesUrl){
        final ArrayList<File> fileArrayList = new ArrayList<>();
        for (String filePath : filesUrl) {
            fileArrayList.add(new File(filePath));
        }
        new Thread() {
            public void run() {
                try {
                    ArrayList<FileItemModel> models;
                    if(serverService!=null){
                        models = serverService.generateItemModels(fileArrayList);
                    }
                    else{
                        models = clientService.generateItemModels(fileArrayList);
                    }

                    for (FileItemModel file : models) {
                        if(serverService!=null){
                            serverService.getFileQueue().put(file);
                        }
                        else{
                            clientService.getFileQueue().put(file);
                        }

                        //fileServer.getFileQueue().notifyAll();
                    }
                    //fileProgressedRequested(fileClient.getFileQueue().size());
                } catch (IOException ex) {
                    Logger.getLogger(FileClientController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FileClientController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }
}
