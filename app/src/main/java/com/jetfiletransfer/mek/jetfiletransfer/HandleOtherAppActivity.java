package com.jetfiletransfer.mek.jetfiletransfer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.jetfiletransfer.mek.jetfiletransfer.connections.FileClient;
import com.jetfiletransfer.mek.jetfiletransfer.connections.FileServer;

import java.util.ArrayList;

public class HandleOtherAppActivity extends AppCompatActivity {
    ArrayList<Uri> imageUris = new ArrayList<>();
    ArrayList<String> urlStrings = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_other_app);
        // Get intent, action and MIME type


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
            else  {
                handleSendImage(intent); // Handle single image being sent
            }

        }
        else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleSendMultipleImages(intent);
        } else {
            // Handle other intents, such as being started from the home screen
        }
        handleRequest();

    }
    void handleRequest(){
        if( isMyServiceRunning(FileClient.class)==true || isMyServiceRunning(FileServer.class)==true){
            Intent intent = new Intent(this, MainActivity.class);
            for(Uri uri: imageUris){
                urlStrings.add(getRealPathFromURI(uri) );

            }
            intent.putStringArrayListExtra("handledValue", (ArrayList<String>) urlStrings);
            startActivity(intent);
            HandleOtherAppActivity.this.finish();
        }
        else{
            Toast.makeText(HandleOtherAppActivity.this,
                    "To share the file you need to connect with the server or client!",
                    Toast.LENGTH_LONG).show();
            HandleOtherAppActivity.this.finish();
        }


    }
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            imageUris.add(imageUri);

        }
    }
    void handleSendMultipleImages(Intent intent) {
        imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}