package com.jetfiletransfer.mek.jetfiletransfer.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.jetfiletransfer.mek.jetfiletransfer.R;
import com.jetfiletransfer.mek.jetfiletransfer.models.FileItemModel;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<FileItemModel>{

    private ArrayList<FileItemModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView fileName;
        TextView speed;
        TextView file_time;
        CircleProgressBar percentiles;
    }

    public CustomAdapter(ArrayList<FileItemModel> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }





    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        FileItemModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.fileName);
            viewHolder.file_time = (TextView) convertView.findViewById(R.id.file_time);
            viewHolder.speed = (TextView) convertView.findViewById(R.id.speed);
            viewHolder.percentiles = (CircleProgressBar) convertView.findViewById(R.id.percentiles);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }




        viewHolder.fileName.setText(dataModel.getFileName());
        viewHolder.file_time.setText(dataModel.getTime());
        viewHolder.speed.setText(dataModel.getTransferSpeed());
        viewHolder.percentiles.setProgress((int)dataModel.getPercentageOfLoadedFile());
        // Return the completed view to render on screen
         Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.down_from_top);
         result.startAnimation(animation);
        return convertView;
    }
}