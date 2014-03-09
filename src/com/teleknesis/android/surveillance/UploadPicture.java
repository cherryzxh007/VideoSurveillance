/*
 * Copyright (c) 2011 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
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
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.*;


/**
 * Here we show uploading a file in a background thread, trying to show
 * typical exception handling and flow of control for an app that uploads a
 * file from Dropbox.
 */
public class UploadPicture extends AsyncTask<Void, Long, Boolean> {

	static String localFile;
	   static String remoteFile;
   
    public UploadPicture(String inp, String out) throws SftpException {
    	
    	localFile = new String(inp);
    	remoteFile = new String(out);
    	
    }

    @Override
    protected Boolean doInBackground(Void... params) {
    	//sshClient = new JSch();
    	
    	
    	Log.i("upload", "in progress");
    	
    	// try uploading file
		try {
			VideoRecorder.sshCh.put(localFile,remoteFile);
			Log.i("upload", remoteFile);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        //int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        //mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //mDialog.dismiss();
       /**
    	if (result) {
            showToast("Image successfully uploaded");
        } else {
            showToast(mErrorMsg);
        }
        */
    }

   
}
