package com.jetfiletransfer.mek.jetfiletransfer;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jetfiletransfer.mek.jetfiletransfer.helpers.CustomAdapter;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModelEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class DownloadFragment extends ListFragment{

    ArrayList<FileItemModel> dataModels= new ArrayList<>();
    private  CustomAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_download, container, false);

        adapter= new CustomAdapter(dataModels,getActivity());
        setListAdapter(adapter);
        //  Retain the ListFragment instance across Activity re-creation
        setRetainInstance(true);
        return rootView;
    }

    public void setItemToListView(FileItemModel model) {
        if(model.getFileItemModelEnum()== FileItemModelEnum.Get){
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
}
