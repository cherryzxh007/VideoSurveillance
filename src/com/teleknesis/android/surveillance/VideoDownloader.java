package com.teleknesis.android.surveillance;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.teleknesis.android.surveillance.utils.Logger;

public class VideoDownloader extends Activity implements OnClickListener, SurfaceHolder.Callback, OnInfoListener  {
	static final String FTP_USER = "khuyen";
	static final String FTP_PASSWORD = "Android789";
	static final String FTP_HOST = "hansa.cs.okstate.edu";
	static final int FTP_PORT = 22;
	
	static JSch sshClient = new JSch();
	static Session session;
	static ChannelSftp sshCh;
	   
    static VideoView cameraView;
    static SurfaceHolder mHolder;
    static ProgressBar mDialog;
    static EditText txtAllFiles; 
    static MediaPlayer mPlayer;
    
    boolean mRecording = false;
    boolean mStop = false;
    boolean mPrepared = false;
    boolean mLoggedIn = false;
	private PowerManager.WakeLock mWakeLock;
	private static ArrayList<String> mFiles = new ArrayList<String>();
    private Timer mStopTimer;   
    String remoteFilePath = "upload/";
    String localFilePath =  "/sdcard/Surveillance/download/";
    Button btnPlay;
    int loopCount = 0;
    static int currentSong;
    static ArrayList<String> filesToDownload = new ArrayList<String>();
    static ArrayList<String> filesForReceiver = new ArrayList<String>();
    ArrayList<String> playedFiles= new ArrayList<String>();
    static AlertDialog alert;
    
     static EditText txtSender;
     static EditText txtReceiver;
     
     String lastFileUploaded;
     static String localSenderID;
     boolean canSaveVideo;
     boolean connectedToServer = false;
     Test qq;
    
    static{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
    }
    
    
@Override
public void onCreate(Bundle savedInstanceState) {
	
	// DON'T MOVE THIS TO FRONT
	//moveTaskToBack(true);
	
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
    
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    
	//************ CREATE NEW MEDIA RECORDER OBJECT ****************
	//************ Camera initialized & put on layout ***************
    setContentView(R.layout.download); 

    cameraView = (VideoView) findViewById(R.id.videoView1);

    mHolder = cameraView.getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	
    
    
	//******************** CREATE BUTTON PLAY **********************
    btnPlay = (Button) findViewById(R.id.buttonPlay);         
    btnPlay.setOnClickListener(this);
    txtAllFiles = (EditText) findViewById(R.id.txtAllFiles);

	
	//******************************************************************

	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Ping");
	mWakeLock.acquire();
	
	//******************************************************************
	
	initDownloader();
	
}


public void initDownloader() {
	// only download video from this sender
		localSenderID = Home.wantedSenderID;
		
		// auto streaming
		try {
			autoStreaming2();
		} catch (IOException e) {		
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
}

public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
	// TODO Auto-generated method stub
	
}
public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	// TODO Auto-generated method stub
	
}
public void surfaceCreated(SurfaceHolder arg0) {
	// TODO Auto-generated method stub
	
}
public void surfaceDestroyed(SurfaceHolder arg0) {
	// TODO Auto-generated method stub
	
}

public boolean connectToServer () {
	try {
		session = sshClient.getSession(FTP_USER, FTP_HOST, FTP_PORT);
	} catch (JSchException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		connectedToServer = false;
		return false;
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
		connectedToServer = false;
		return false;
	}
	Log.i("download", "session-p1 done");
	
	// part 2
	Channel channel;
	try {
		channel = session.openChannel("sftp");
		channel.connect();
		sshCh = (ChannelSftp) channel;    
		Log.i("download", "channel-p2 done");
	} catch (JSchException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		connectedToServer = false;
		return false;
	}
	connectedToServer = true;
	return true;
	
}

@SuppressWarnings("unchecked")
public void checkFilesFromServer() {
		// connect to server
		boolean connected = connectToServer();
		
		// get videos according to receiver's username
		if (connected) {
			String localReceiverID = Home.txtSender.getText().toString(); //txtSender acts as receiver's name in this case
			Log.i("getfiles", localReceiverID);
			Vector<ChannelSftp.LsEntry> allFiles;
			try {
				sshCh.cd(remoteFilePath);
				allFiles = (Vector<ChannelSftp.LsEntry>) (sshCh.ls("*.mp4"));
				
				// get list of files send to the this receiver
				for (ChannelSftp.LsEntry entry:allFiles) {
					// get the receiver's name from server files
					String remoteReceiverID = getReceiverID(entry.getFilename());
					
					// create a list of files for this receiver from both wanted & unwanted senders
					filesForReceiver = new ArrayList<String>(); 
					
					Log.i("getfiles", Boolean.toString(remoteReceiverID.equals(localReceiverID)));
					
					
					// get only the files that have local receiver's ID
					if (remoteReceiverID.equals(localReceiverID)) {
						filesForReceiver.add(entry.getFilename());		
						
					}
				}
				
	
			} catch (SftpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Collections.sort(filesForReceiver);
			
		}
		
		else {
			Log.i("error","Could not connect to server");
		}
}

public void downloadVideosFromServer() {
		  File mediaStorageDir = new File(localFilePath);
		    if ( !mediaStorageDir.exists() ) {
		        if ( !mediaStorageDir.mkdirs() ){
		            Logger.Debug("failed to create directory");
		        	}
		    	}

		   boolean connected = sshCh.isConnected();
		    
		   
		    if (connected) {
				try {
					for (String fileName : filesToDownload) {
						DownloadFile.mFileLen = filesToDownload.size();
						new DownloadFile(fileName, localFilePath + fileName);
						
						txtAllFiles.setText(txtAllFiles.getText() + "\n" + localFilePath + fileName);
					}
					
				} catch (SftpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
}
/**
public void autoStreaming() {
		// get a list of files to download
		checkFilesFromServer();
		
		// download videos
		downloadVideosFromServer();
		    
		// play videos
		playVideos();
}
*/

public void getFilesFromServer()  throws IOException, SftpException, InterruptedException  {
	 boolean connected = sshCh.isConnected();
	 Log.i("files to download size", Integer.toString(filesToDownload.size()));
	 if (connected) {
		 		String fileName = filesToDownload.get(currentSong);
		 		while (playedFiles.contains(fileName)) {
		 			currentSong++;
		 			fileName = filesToDownload.get(currentSong);
		 		}
		 		
		 		// see if videos can be saved
	 			if (fileName.contains("_s.mp4")) {
	 				canSaveVideo = true;
	 			} else {
	 				canSaveVideo = false;
	 			}
	 			
	 			// download new file
		 		qq = new Test(fileName); 
		 		
		 		// play downloaded video
		 		cameraView.setVideoPath(getDataSource(qq.stream));
		 		playedFiles.add(fileName);
		 		
		 		if (qq.isDone) {
		 			playVideo2();
		 		}
			
	}
	
}

public void autoStreaming2() throws IOException, SftpException, InterruptedException {
	// get list of files from WANTED receiver only
	for (String file:filesForReceiver) {
		String remoteSenderID = getSenderID(file);
		
		if (remoteSenderID.equals(localSenderID)) { 
			if (!filesToDownload.contains(file) && (!new File (localFilePath + file).exists())) {
				filesToDownload.add(file);
				Log.i("filesToDownload.size()", Integer.toString(filesToDownload.size()));
			}
		}
	}		
	if (filesToDownload.size() > 0) {
		getFilesFromServer();
	}
}

private void playVideo2() {
	try {
			if (!qq.stream.equals(null)) {
				
				txtAllFiles.setText(qq.fileName);
				
				cameraView.start();
				cameraView.requestFocus();
				cameraView.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						 try {
							 checkFilesFromServer();
							 Log.i("stream", "currentSong" + currentSong);
							 if (currentSong + 1 < filesToDownload.size()) {
								 currentSong++;
								 getFilesFromServer();
								 cameraView.start();
								 cameraView.requestFocus();
							 }
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SftpException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
				});
			}
			else {
				Log.i("Stream", "null stream");
			}

	} catch (Exception e) {
		Log.e("tag", "error: " + e.getMessage(), e);
		if (cameraView != null) {
			cameraView.stopPlayback();
		}
	}
}

private String getDataSource(InputStream stream) throws IOException {
	
	if (stream == null)
		throw new RuntimeException("stream is null");
	
	File temp;
	
	if (!canSaveVideo) {
		temp = File.createTempFile("mediaplayertmp", ".mp4");
		temp.deleteOnExit();
	}
	else {
		 File dir = new File(localFilePath);
		 if (!dir.exists()) {
			 dir.mkdirs();
		 }
		temp = new File(localFilePath + "VID_" + localSenderID + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4");
	}
	

	String tempPath = temp.getAbsolutePath();
	FileOutputStream out = new FileOutputStream(temp);
	byte buf[] = new byte[128000];
	do {
		int numread = stream.read(buf);
		if (numread <= 0)
			break;
		out.write(buf, 0, numread);
	} while (true);
	try {
		stream.close();
	} catch (IOException ex) {
		Log.e("tag", "error: " + ex.getMessage(), ex);
	}
	return tempPath;
}

public void onClick(View arg0) {
	
	try {
		autoStreaming2();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (SftpException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}

/**
OnCompletionListener completionListener=new OnCompletionListener() {
	public void onCompletion(MediaPlayer mp) {
		
		Log.i("blah", Integer.toString(currentSong));
		
		 File vidDir = new File(localFilePath);
		    if ( !vidDir.exists() ) {
		        if ( !vidDir.mkdirs() ){
		            Logger.Debug("failed to create directory");
		        	}
		    	}
		    File[] vidFiles = vidDir.listFiles();
			System.out.println(vidFiles.length);
		
			if (currentSong == vidFiles.length) {
				mp.release();
				return;
			}
		
		VideoPlayerSetup(mPlayer,vidFiles[currentSong].getPath());
		mPlayer.prepareAsync();
		currentSong++;
	};

};

public void VideoPlayerSetup(MediaPlayer mPlayer, String dataSource) {
	try {
		Log.i("blah", dataSource);
		mPlayer.reset();
		mPlayer.setDataSource(dataSource);
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		//mPlayer.prepare();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	txtAllFiles.setText(dataSource);
}

OnPreparedListener preparedListener = new OnPreparedListener() {
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.start();
	}
};

public void playVideos() {
	mPlayer = new MediaPlayer();
	mPlayer.setDisplay(mHolder);   
	mPlayer.setOnCompletionListener(completionListener);
	mPlayer.setOnPreparedListener(preparedListener);
	
	txtAllFiles.setText("");
	currentSong = 0;
	
	completionListener.onCompletion(mPlayer);
}

public void TestPlayingVideo(View v) {
	playVideos();
}
*/

public String getSenderID(String filename) {
	int _1stPos = 0;
	int _2ndPos = 0;
	for (int i = 0; i < filename.length(); i++) {
		if (filename.charAt(i) == '_') {
			_1stPos = i;
			for (int j = i+1; j < filename.length(); j++) {
				if (filename.charAt(j) == '_') {
					_2ndPos = j;
					break;
				}
			}
			if (_1stPos != 0) break;
		}
	}
	return (filename.substring(_1stPos+1,_2ndPos));
}	

public String getReceiverID(String filename) {
	int startPos = filename.indexOf(getSenderID(filename));
	
	int _1stPos = 0;
	int _2ndPos = 0;
	
	for (int i = startPos; i < filename.length(); i++) {
		if (filename.charAt(i) == '_') {
			_1stPos = i;
			for (int j = i+1; j < filename.length(); j++) {
				if (filename.charAt(j) == '_') {
					_2ndPos = j;
					break;
				}
			}
			if (_1stPos != 0) break;
		}
	}
	return (filename.substring(_1stPos+1,_2ndPos));
}	


@Override
protected void onStop() {
 super.onStop();
 if (alert != null) {
  alert.dismiss();
  alert = null;
 }
}
}