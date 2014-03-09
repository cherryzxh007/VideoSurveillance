package com.teleknesis.android.surveillance;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.teleknesis.android.surveillance.utils.Logger;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;

public class CreateVid extends AsyncTask<MediaRecorder, Void, Void> {
	MediaRecorder mRecorder;
	String filePath;
	
	public CreateVid (MediaRecorder m, String path) {
		mRecorder = m;
		filePath = path;
	}
	
	private void initRecorder() { // called everytime new video is recorded
		mRecorder.setMaxDuration(2000);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
		mRecorder.setProfile(profile);
		
		// for debugging
		Logger.Debug("start init Recorder");
		
	    //mRecorder.setOutputFile(filePath);

		// for debugging
	    Logger.Debug("stop init Recorder");
	}

	@Override
	protected Void doInBackground(MediaRecorder... params) {
		// TODO Auto-generated method stub
		
		//mRecorder = params[0];
		//mRecorder.reset();
		
		//initRecorder();
		
		
		//prepareRecorder();
		try {
			//initRecorder();
			//VideoRecorder.initRecorder();
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mRecorder.start();
		return null;
	}
	


}
