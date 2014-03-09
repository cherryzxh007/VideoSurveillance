/**
 * DOWNLOAD USING FTP
 * CURRENTLY NOT IN USE
 */


package com.teleknesis.android.surveillance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jcraft.jsch.*;


/**
 * Here we show uploading a file in a background thread, trying to show
 * typical exception handling and flow of control for an app that uploads a
 * file from Dropbox.
 */
public class DownloadFile extends AsyncTask<Void, Long, Boolean> {

	static String localFile;
	static String remoteFile;
	ProgressBar mDialog = VideoDownloader.mDialog;
	static int mFileLen;
   

	public DownloadFile(String inp, String out) throws SftpException {
    	
    	localFile = new String(out);
    	remoteFile = new String(inp);
    	Log.i("download", this.doInBackground().toString());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
    	//sshClient = new JSch();
    	
    	Log.i("download", "in progress");
    	
    	// try uploading file
		try {
			VideoDownloader.sshCh.get(remoteFile, localFile);
			Log.i("download", localFile);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        ((DialogInterface) mDialog).dismiss();
       
    	if (result) {
            //Toast.makeText(, text, duration)
        } else {
            //showToast(mErrorMsg);
        }
       
    }

   
}
