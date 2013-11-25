package org.thinav.filemonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.thinav.filemonitor.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.widget.Toast;

public class FileMonitorService extends Service {
    //The log for results
    private ArrayList<String[]> mLog;

    //Holds the root directory we want to watch
    private String mWatchDir;

    //Flat list of all the FileObserver objects we maintain
    private ArrayList<FileObserverNode> mTreeNodes;

    //Used for determining where to save
    private boolean mSaveExt = false;

    //Service ID
    private int mNotifyId = 3141;

    @Override
    public IBinder onBind(Intent intent) {
        return(null);
    }

    //methods to start and stop the observer objects in the list
    private void startWatchingTree(){
        for (FileObserverNode fon: mTreeNodes){
            fon.startMyObserver();
        }
    }
    private void stopWatchingTree(){
        for (FileObserverNode fon: mTreeNodes){
            fon.stopMyObserver();
        }
    }

    //Attach a new FileObserverNode
    private void attachNode(String path, int observerMask, ArrayList<String[]> logCopy){
        //Is this the root directory we are supposed to watch?
        if (path.equalsIgnoreCase(mWatchDir)){
            //Create a new observer
            FileObserverNode fon = new FileObserverNode(path, observerMask, logCopy);
            //Add it too the list
            mTreeNodes.add(fon);
        }
        //Open the directory
        File f = new File(path);
        //Get a list of all the files
        File[] dirs = f.listFiles();
        if (dirs != null){
            for (File child: dirs){
                //For every file which is actually a directory...
                if (child.isDirectory()){
                    //Check that it not the virtual directory (/proc) or the system directory, because
                    //attempting to watch these will create a race condition.
                    if ((!child.getAbsolutePath().equalsIgnoreCase("/proc")) && (!child.getAbsolutePath().equalsIgnoreCase("/sys"))){
                        //Create the node and add it to the list
                        FileObserverNode fon = new FileObserverNode(child.getAbsolutePath(), observerMask, logCopy);
                        mTreeNodes.add(fon);
                        //Recursively call this function with the directory name, producing a depth first traversal
                        attachNode(child.getAbsolutePath(), observerMask, logCopy);
                    }
                }
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            //Make this service a foreground service (take directly from SDK docs)
            Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.notification_short), System.currentTimeMillis());
            notification.flags|=Notification.FLAG_NO_CLEAR;
            Intent notificationIntent = new Intent(this, FileMonitorActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            notification.setLatestEventInfo(this, getText(R.string.notification_short),getText(R.string.notification_long), pendingIntent);
            startForeground(mNotifyId, notification);

            //Instantiate the log & the observer tree
            mLog = new ArrayList<String[]>();
            mTreeNodes = new ArrayList<FileObserverNode>();

            //Pluck the data from the intent
            Bundle b = intent.getExtras();
            mSaveExt = b != null ? b.getBoolean(FileMonitorActivity.mExtraExtern) : false;
            mWatchDir = b != null ? b.getString(FileMonitorActivity.mExtraWatch) : "/";

            //Attach & activate monitors
            int observerFlags = FileObserver.OPEN | FileObserver.MODIFY; //These flags define what activities we are looking for
            attachNode(mWatchDir, observerFlags, mLog);
            startWatchingTree();
        }catch(Exception e){
            Toast.makeText(this, getText(R.string.toast_service_error_starting) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, getText(R.string.toast_service_started) + String.valueOf(mTreeNodes.size()), Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopWatchingTree();
        //save log data
        try{
            //get file to write
            ArrayList<String[]> log = mLog; //Performance best practice
            FileOutputStream fos;
            if (mSaveExt){
                File save = new File (Environment.getExternalStorageDirectory(), FileMonitorActivity.mSaveFile);
                fos = new FileOutputStream(save);
            }else{
                fos = openFileOutput(FileMonitorActivity.mSaveFile, Context.MODE_WORLD_READABLE);
            }
            //iterate and write log to a file
            for (int i=0; i < log.size(); i++){
                String[] t = (String[]) log.get(i);
                String t2 = t[0] + "," + t[1] + "," + t[2] + "," + t[3] + "\n";
                fos.write(t2.getBytes());
            }
            fos.close();
        }catch(Exception e){
            Toast.makeText(this, getText(R.string.toast_service_error) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this, getText(R.string.toast_service_stop) + String.valueOf(mLog.size()), Toast.LENGTH_SHORT).show();
        stopForeground(true);
        super.onDestroy();
    }
}