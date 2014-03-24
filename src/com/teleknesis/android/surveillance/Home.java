package com.teleknesis.android.surveillance;

import java.io.File;
import java.io.IOException;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;

import com.jcraft.jsch.SftpException;
import com.teleknesis.android.surveillance.utils.Logger;

public class Home extends Activity {
	static CheckBox chkSaveVideo;
	static EditText txtSender;
	static EditText txtReceiver;
	public static String wantedSenderID;
	public static String receiverID;
	private MediaRecorder mRecorder;
	private Handler handler = new Handler();
	private AlertDialog alert;
	private boolean alertShowed = false;
	private VideoDownloader vd;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);
			mRecorder = new MediaRecorder();
			txtSender = (EditText) findViewById(R.id.txtSender);
			txtReceiver = (EditText) findViewById(R.id.txtReceiver);
			chkSaveVideo = (CheckBox) findViewById(R.id.chkSaveVideo);
			
			vd = new VideoDownloader();
			
			/*
			// start timer for checking new vids
			 * 
			 */
			handler.postDelayed(runnable, 100);
			
			// for testing allowing to save video
			chkSaveVideo.setChecked(true);

			} 
        catch (Exception error) {
			Logger.Error( "Home_onCreate", error );
			}
    	}
    
    
    public void LaunchCameraActivity( View v ) {
    	startActivityForResult( new Intent(Home.this, VideoRecorder.class), 1000);
    }       
    
    @SuppressWarnings("static-access")
	public void LaunchDownloader (View v) throws IOException, SftpException, InterruptedException {
    	//checkNewVideos();
    	//startActivityForResult(new Intent(Home.this, VideoDownloader.class), 1000);
    }
    

    private Runnable runnable = new Runnable() {
       public void run() {
         
    	   
          try {
			checkNewVideos();
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
          
     	  //Toast.makeText(getApplicationContext(),""+ System.currentTimeMillis(), Toast.LENGTH_LONG).show();
          /* and here comes the "trick" */
          handler.postDelayed(this, 1000);
       }
    };
    
    @SuppressWarnings("static-access")
	public void checkNewVideos() throws IOException, SftpException, InterruptedException {
    	if (vd.connectedToServer == false) {
    		vd.checkFilesFromServer(); // only call this if timer is disabled
    		vd.initDownloader(); // this call autostreaming2
    		while (vd.filesForReceiver.size() < 1) {
    				vd.checkFilesFromServer();
    		}
    	}
    	
    	if (vd.filesForReceiver.size() > 0) {
			if (!alertShowed) {
				showAlert();
			}
		}
    }
    
    public void showAlert() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("New video");
	    
	    // get the name of the newest sender
	    final String remoteSenderID = vd.getSenderID(vd.filesForReceiver.get(vd.filesForReceiver.size()-1));
	    builder.setMessage("Do you want to watch new video from " + remoteSenderID + " ?");
	    
	    // Yes clicked
	    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	wantedSenderID = remoteSenderID;
	        	startActivityForResult(new Intent(Home.this, VideoDownloader.class), 1000);
	            dialog.dismiss();
	        }

	    });

	    // NO clicked
	    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

	        public void onClick(DialogInterface dialog, int which) {
	            // Do nothing
	            dialog.dismiss();
	        }
	    });

	    
	    alert = builder.create();
	    alert.show();
	    alertShowed = true;
    }
    
    public void onStop() {
    	 super.onStop();
    	 if (alert != null) {
    	  alert.dismiss();
    	  alert = null;
    	 }
    	 alertShowed = false;
    	}
    
    public void onDestroy() {
    	super.onDestroy();
    	handler.removeCallbacks(runnable);
    }
    
    public void onResume() {
    	// start timer for checking new vids
    	super.onResume();
    	//handler.removeCallbacks(runnable);
    	//handler.postDelayed(runnable, 1000);
    }
}