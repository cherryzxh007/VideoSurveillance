package com.teleknesis.android.surveillance;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;

import com.teleknesis.android.surveillance.utils.Logger;

public class Home extends Activity {
	
	static EditText txtSender;
	static EditText txtReceiver;
	public static String senderID;
	public static String receiverID;
	
	private MediaRecorder mRecorder;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);
			mRecorder = new MediaRecorder();
			txtSender = (EditText) findViewById(R.id.txtSender);
			txtReceiver = (EditText) findViewById(R.id.txtReceiver);
			
			// for dgb
			//new UploadFile("sdcard/Surveillance/VID_20131225_234903.mp4", "/upload/VID_20131225_234903.mp4");

			
			} 
        catch (Exception error) {
			Logger.Error( "Home_onCreate", error );
			}
    	}
    
    
    public void LaunchCameraActivity( View v ) {
    	startActivityForResult( new Intent(Home.this, VideoRecorder.class), 1000);
    }       
    
    public void LaunchDownloader (View v) {
    	startActivityForResult(new Intent(Home.this, VideoDownloader.class), 1000);
    }
}