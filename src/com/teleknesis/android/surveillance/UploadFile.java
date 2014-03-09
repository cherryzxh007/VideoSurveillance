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
public class UploadFile {

   final String FTP_USER = "khuyen";
   final String FTP_PASSWORD = "Android789";
   final String FTP_HOST = "hansa.cs.okstate.edu";
   final int FTP_PORT = 22;
   JSch sshClient = new JSch();
   String localFile;
   String remoteFile;
   
    public UploadFile(String inp, String out) throws SftpException {
    	
    	//sshClient = new JSch();
    	localFile = new String(inp);
    	remoteFile = new String(out);
    	
    	Log.i("upload", "in progress");
    	
    	try {
			// try to connect
    			
    			// part 1
    			Session session = sshClient.getSession(FTP_USER, FTP_HOST, FTP_PORT);
    			java.util.Properties config = new java.util.Properties();
    	        config.put("StrictHostKeyChecking", "no");
    			session.setConfig(config);
    			session.setPassword(FTP_PASSWORD);
    			session.connect();
    			Log.i("upload", "session-p1 done");
    			
    			// part 2
    			Channel channel = session.openChannel("sftp");
    			channel.connect();
    			ChannelSftp sshCh = (ChannelSftp) channel;    
    			Log.i("upload", "channel-p2 done");
				
    			// try uploading file
    			try {
					//sshCh.put(localFile, remoteFile);
    				//sshCh.connect();
    				//sshCh.cd("upload");
    				sshCh.put(localFile,remoteFile);
					Log.i("upload", remoteFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		
    			// done uploading file
    			session.disconnect();
    			Log.i("upload", "all done");
    			
    			
    	
    	} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
       
    }

    
}
