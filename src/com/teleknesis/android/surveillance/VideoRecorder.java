package com.teleknesis.android.surveillance;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.teleknesis.android.surveillance.utils.Logger;

//*******************************************************
//*******************************************************
// CamaraView
//*******************************************************
//*******************************************************
//@SuppressLint("NewApi")

public class VideoRecorder extends Activity implements OnClickListener, SurfaceHolder.Callback, OnInfoListener  {
	static final String FTP_USER = "khuyen";
	static final String FTP_PASSWORD = "Android789";
	static final String FTP_HOST = "hansa.cs.okstate.edu";
	static final int FTP_PORT = 22;
	   static JSch sshClient = new JSch();
	   
	   static Session session;
	   static ChannelSftp sshCh;
    static MediaRecorder mRecorder;
    static SurfaceHolder mHolder;
    boolean mRecording = false;
    boolean mStop = false;
    boolean mPrepared = false;
    boolean mLoggedIn = false;
	private PowerManager.WakeLock mWakeLock;
	private static ArrayList<String> mFiles = new ArrayList<String>();
    private Timer mStopTimer;   
    String remoteFilePath = "upload/";
    Button btnCapture;
    int loopCount = 0;
    
     static EditText txtSender;
     static EditText txtReceiver;
     static CheckBox chkSaveVideo;
     
     String lastFileUploaded;
    
    static{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
    }
    
    
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
    
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    
	//************ CREATE NEW MEDIA RECORDER OBJECT ****************
	//************ Camera initialized & put on layout ***************
    mRecorder = new MediaRecorder();	
    mRecorder.setOnInfoListener(this);
    initRecorder();
    setContentView(R.layout.camera);


    SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface_camera);
    mHolder = cameraView.getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    
	//******************** CREATE BUTTON CAPTURE **********************
   
    chkSaveVideo = (CheckBox) findViewById(R.id.chkSaveVideo);
    txtSender = (EditText) findViewById(R.id.txtSender);
    txtReceiver = (EditText) findViewById(R.id.txtReceiver);
    btnCapture = (Button) findViewById(R.id.btnCapture);         
    btnCapture.setOnClickListener(this);
    

	//******************************************************************

	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Ping");
	mWakeLock.acquire();

}



@Override
protected void onDestroy() {
    try {
    	super.onDestroy();
        Logger.Debug("onStop");
		mWakeLock.release();
		} 
    catch (Exception e) {
		Logger.Error( "VideoRecorderTest_onStop", e );
		}
	}


@Override
public void onResume() {
	super.onResume();
}

public static void initRecorder() { // called everytime new video is recorded
	
	mRecorder.setMaxDuration(2000);
	mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
	CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
	mRecorder.setProfile(profile);
	
	
	// for debugging
	Logger.Debug("start init Recorder");

	// check & create video directory
    File mediaStorageDir = new File("/sdcard/Surveillance/");
    if ( !mediaStorageDir.exists() ) {
        if ( !mediaStorageDir.mkdirs() ){
            Logger.Debug("failed to create directory");
        	}
    	}

	// set each video with a new file name
    //txtSender.setText("KDuong");
    String senderID = Home.txtSender.getText().toString();
    String receiverID = Home.txtReceiver.getText().toString();
    String filePath = "/sdcard/Surveillance/" + "VID_" + senderID + "_" + receiverID + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    
    if (Home.chkSaveVideo.isChecked()) {
    	filePath = filePath + "_s"; // show that video should be saved
    }
    		
    filePath = filePath + ".mp4";
    mFiles.add(filePath);
    mRecorder.setOutputFile(filePath);

	// for debugging
    Logger.Debug("stop init Recorder");
}

private void prepareRecorder() {
	Logger.Debug( "About to prepare" );
	mRecorder.setPreviewDisplay(mHolder.getSurface());
	
    try {
        				mRecorder.prepare(); 
        				mPrepared = true;
        				Logger.Debug( "right after prepare");
    } catch (IllegalStateException e) {
        Logger.Error("prepareRecorder_IllegalStateException", e);
        //finish();
    } catch (IOException e) {
    	Logger.Error("prepareRecorder_IOException", e);
        //finish();
    } catch( Exception e) {
    	Logger.Error("prepareRecorder", e);
    	}
    Logger.Debug("after prepare");
    
}


public void onClick(View v) {
	
    try {
		if (mRecording) {
			Logger.Debug("Stopping");
			Toast.makeText(getApplicationContext(), "Stopclicked", Toast.LENGTH_SHORT).show();
			mRecorder.stop();
			mStopTimer.cancel();
			mPrepared = false;
			mRecording = false;
			
			 //Thread.currentThread().interrupt(); <~~ doesn't let user clicking the stop button
			
			String lastLocalFile = new File (mFiles.get(mFiles.size()-2)).getName();
			do {
				//btnCapture.setText("Uploading file - Please don't start new record yet");
				//Toast.makeText(getApplicationContext(), "Stopclicked", Toast.LENGTH_SHORT).show();
				restartRecorder();
			} while (lastLocalFile != lastFileUploaded);
			
			btnCapture.setText("Start");
			session.disconnect();
			Toast.makeText(getApplicationContext(), "canStopnow", Toast.LENGTH_SHORT).show();
			Log.i("upload", "all done");
			
			mRecorder.reset();
			this.initRecorder();
			this.prepareRecorder();
			
			} 
		else {
			
			try {
				session = sshClient.getSession(FTP_USER, FTP_HOST, FTP_PORT);
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			java.util.Properties config = new java.util.Properties();
		    config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(FTP_PASSWORD);
			try {
				session.connect();
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("upload", "session-p1 done");
			
			// part 2
			Channel channel;
			try {
				channel = session.openChannel("sftp");
				channel.connect();
				sshCh = (ChannelSftp) channel;    
				Log.i("upload", "channel-p2 done");
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			btnCapture.setText("Stop");
			mRecorder.start();
			mRecording = true;
	}
}
    
    catch (Exception e) {
		Logger.Error( "onClick", e );
		}
	}

public void restartRecorder() {
				loopCount ++;
				//Toast.makeText(getApplicationContext(), "COUNT: " + loopCount, Toast.LENGTH_LONG).show();
				
				/*
				Logger.Debug("'bout to restart recorder");
				btnCapture.setText("Start");
				mRecorder.reset();
				mPrepared = false;
				mRecording = false;
				
				initRecorder();
				prepareRecorder();
				mRecorder.start();
				mRecording = true;
				btnCapture.setText("Stop");
				*/
				mRecording = true;
				if (mFiles.size() >= 2) {
					//Start uploading the last file
					//new UploadPicture(VideoRecorder.this, mApi, "/", new File(mFiles.get(mFiles.size()-2)) ).execute();
					File newFile = new File(mFiles.get(mFiles.size()-2));
					
					String remoteFile = remoteFilePath + newFile.getName();
					
					try {
						Log.i("upload", newFile.getAbsolutePath());
						//new UploadFile();
						Log.i("upload",new UploadPicture(newFile.getAbsolutePath(), remoteFile).execute().toString());
						Log.i("upload",remoteFile);
						lastFileUploaded = remoteFile;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
}


public void surfaceCreated(SurfaceHolder holder) {
	if( !mPrepared ) {
		prepareRecorder();
		}
	}

public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

public void surfaceDestroyed(SurfaceHolder holder) {
	 mRecorder.release();
	 
	/** 
    if (mRecording) {
    	
    	mStopTimer.cancel();
        mRecorder.stop();
        mRecording = false;
        
        String lastLocalFile = new File (mFiles.get(mFiles.size()-2)).getName();
		do {
			//btnCapture.setText("Uploading file - Please don't start new record yet");
			//Toast.makeText(getApplicationContext(), "Stopclicked", Toast.LENGTH_SHORT).show();
			restartRecorder();
		} while (lastLocalFile != lastFileUploaded);
        //mRecorder.release();
    	}
   
    // done uploading file
	
	*/
}

	public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
		if (arg1 == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			restartRecorder();
			mRecorder.reset();
			initRecorder();
			new CreateVid(mRecorder, mFiles.get(mFiles.size()-2)).execute();
		}
	}

}