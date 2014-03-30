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
	
	@Override
	protected Void doInBackground(MediaRecorder... params) {
		try {
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mRecorder.start();
		return null;
	}
	


}
