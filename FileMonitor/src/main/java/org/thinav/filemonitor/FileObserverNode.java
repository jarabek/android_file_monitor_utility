/*
 *  Written by: Chris Jarabek (chris [dot] jarabek [at] gmail [dot] com)
 *  
 *  Last modified: November 24, 2011
 * 
 */

package org.thinav.filemonitor;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

import android.os.FileObserver;

public class FileObserverNode {
    //Path this object is watching
    private String mWatchPath;

    //The actual instantiated observer class
    private FileObserver mPathObserver;

    //Copy of the log where things are being recorded
    private ArrayList<String[]> mLogCopy;

    //I just did this so that this code would have similar output to another logging tool I wrote in Python.
    private static String mOpen = "IN_OPEN";
    private static String mModify = "IN_MODIFY";

    public FileObserverNode(String path, int observerMask, ArrayList<String[]> log){
        mWatchPath = path;
        mLogCopy = log;
        mPathObserver = new FileObserver(path, observerMask) {
            @Override
            //When we see an event
            public void onEvent(int event, String path) {
                //parse name
                String eventName = "";
                if (event == FileObserver.OPEN)
                    eventName = mOpen;
                else if (event == FileObserver.MODIFY)
                    eventName = mModify;

                if ((path != null) && (path.length() > 0) && (!(eventName.length()==0))){
                    //get size
                    String fullPath = mWatchPath + "/" + path;
                    File f = new File(fullPath);
                    long size = f.length();

                    //get time of the event
                    long time = System.currentTimeMillis();
                    BigDecimal bdTime = new BigDecimal(time);
                    BigDecimal thousand = new BigDecimal(1000);
                    BigDecimal ftime = bdTime.divide(thousand);

                    //create entry
                    String[] entry = {fullPath, String.valueOf(size), eventName, ftime.toString()};
                    mLogCopy.add(entry);
                }
            }
        };
    }
    public void startMyObserver(){
        mPathObserver.startWatching();
    }
    public void stopMyObserver(){
        mPathObserver.stopWatching();
    }

}
