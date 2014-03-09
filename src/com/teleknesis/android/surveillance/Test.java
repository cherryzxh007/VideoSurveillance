package com.teleknesis.android.surveillance;
/**
 * DOWNLOAD USING FTP
 * CURRENTLY NOT IN USE
 */


import java.io.*;


import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.*;


/**
 * Here we show uploading a file in a background thread, trying to show
 * typical exception handling and flow of control for an app that uploads a
 * file from Dropbox.
 */
public class Test {

	static String localFile;
	static String remoteFile;
	static int mFileLen;
	static final String FTP_USER = "khuyen";
	static final String FTP_PASSWORD = "Android789";
	static final String FTP_HOST = "hansa.cs.okstate.edu";
	static final int FTP_PORT = 22;
	String testFolder = "upload/";
	//static String testFile = "test.mp4";
	static JSch sshClient = new JSch();
	   
	   static Session session;
	   static ChannelSftp sshCh;
	    boolean isDone = false;
	    InputStream stream;
   

	public Test(String testFile) throws IOException, SftpException {
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
		
		
		// part 2
		Channel channel;
		try {
			channel = session.openChannel("sftp");
			channel.connect();
			sshCh = (ChannelSftp) channel;    
			

			int bytesRead;
		    int current = 0;
		    FileOutputStream fos = null;
		    BufferedOutputStream bos = null;
		    ChannelSftp sock = sshCh;
		  
		    sock.cd(testFolder);
		    stream = sock.get(testFile);
		    Log.i("stream", "successfully get file");
		    isDone = true;
		    
		    /**
		    try {
		     
		      System.out.println("Connecting...");
		      
		      
		      // receive file
		      byte [] mybytearray  = new byte [10000000];
		      InputStream is = sock.get(testFile);
		      
		      File fileReceived = File.createTempFile("abc", ".jpg", new File("abc"));
		      fos = new FileOutputStream(fileReceived);
		      bos = new BufferedOutputStream(fos);
		      bytesRead = is.read(mybytearray,0,mybytearray.length);
		      current = bytesRead;

		      do {
		         bytesRead =
		            is.read(mybytearray, current, (mybytearray.length-current));
		         if(bytesRead >= 0) current += bytesRead;
		      } while(bytesRead > -1);

		      bos.write(mybytearray, 0 , current);
		      bos.flush();
		      System.out.println("File " + testFile
		          + " downloaded (" + current + " bytes read)");
		      System.out.println(fileReceived.getAbsolutePath());
		    }
		    finally {
		      if (fos != null) fos.close();
		      if (bos != null) bos.close();
		      if (sock != null) sock.disconnect();
		    }
		  
			**/
		   // sock.disconnect();
			
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
   
}