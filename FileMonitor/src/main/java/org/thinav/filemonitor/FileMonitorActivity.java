package org.thinav.filemonitor;

import java.io.File;

import org.thinav.filemonitor.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FileMonitorActivity extends Activity {

    //Internal toggle so the activity knows if the service is running
    private boolean mIsRunning;

    //Object references to view elements
    private Button mToggleButton;
    private EditText mEditText;
    private TextView mToggleText;

    //Default directory, and file output name
    private static String mWatchDir = "/mnt";
    protected static String mSaveFile = "monitor_log.csv";

    //Some stuff for intents
    protected static String mExtraWatch = "WATCH_DIR";
    protected static String mExtraExtern = "SAVE_EXT";

    //For determining if we can write to the SD card
    private boolean mExternalStorageAvailable = false;
    private boolean mExternalStorageWriteable = false;
    String state = Environment.getExternalStorageState();


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.watch_dir);
        mEditText.setText(mWatchDir);

        //Check if we can write to the SD card (take from Google's SDK examples)
        String loc = "";
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable && mExternalStorageWriteable){
            File f = Environment.getExternalStorageDirectory();
            loc = f.getAbsolutePath() + "/" + mSaveFile;
        }
        else
            loc = getFilesDir().toString() + "/" + mSaveFile;


        //Adjust the save location accordingly
        TextView savedLoc = (TextView) findViewById(R.id.file_loc);
        savedLoc.setText(loc);


        //Grab the elements we want to toggle
        mToggleText = (TextView) findViewById(R.id.status);
        mToggleButton = (Button) findViewById(R.id.toggle);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //check if directory is valid
                mWatchDir = mEditText.getText().toString();
                File check = new File(mWatchDir);
                if (check.isDirectory()){
                    //All is good, start or stop the service
                    toggleService();
                }else{
                    Toast.makeText(getApplicationContext(), mWatchDir + getText(R.string.toast_bad_dir), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int toggleService(){
        int retVal = 0;
        //check if running
        if (!mIsRunning) {
            //start service
            Intent intent = new Intent(this, FileMonitorService.class);
            if (mExternalStorageAvailable && mExternalStorageWriteable)
                intent.putExtra(mExtraExtern, true);
            else
                intent.putExtra(mExtraExtern, false);
            intent.putExtra(mExtraWatch, mWatchDir);
            startService(intent);
            mIsRunning = true;

            //modify view
            mToggleButton.setText(R.string.stop);
            mToggleText.setText(R.string.running);
        }else{
            //stop service
            Intent intent = new Intent(this, FileMonitorService.class);
            stopService(intent);
            mIsRunning = false;

            //modify view
            mToggleButton.setText(R.string.start);
            mToggleText.setText(R.string.not_running);
        }
        return retVal;
    }
}


