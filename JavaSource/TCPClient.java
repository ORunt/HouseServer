package com.camshouse;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
 
public class TCPClient {
 
    private String serverMessage;
    public static final String SERVERIP = 192.168.3.68;//"192.168.1.101"; //your computer IP address
    public static final int SERVERPORT = 8080;//4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    Socket socket;
 
    PrintWriter out;
    BufferedReader in;
    
    //constructor
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }
    
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }
    
    public void stopClient(){
    	try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        mRun = false;
    }
 
    public void run() {
    	boolean mSocketFailure = true;
        mRun = true;

        while(mSocketFailure)
        {
        	mSocketFailure = false;
	        try {
	            //computer's IP address.
	            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
	            Log.e("TCP Client", "C: Connecting...");
	            //create a socket to make the connection with the server
	            socket = new Socket(serverAddr, SERVERPORT);
	 
	            try {
	            	mSocketFailure = false;
	                //send the message to the server
	                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true); 
	                Log.e("TCP Client", "C: Sent."); 
	                Log.e("TCP Client", "C: Done.");
	 
	                //receive the message which the server sends back
	                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	 
	                //in this while the client listens for the messages sent by the server
	                while (mRun) {
	                    serverMessage = in.readLine();
	 
	                    if (serverMessage != null && mMessageListener != null) {
	                        //call the method messageReceived from MyActivity class
	                        mMessageListener.messageReceived(serverMessage);
	                    }
	                    serverMessage = null;
	                }
	 
	                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'"); 
	            }
	            
	            catch (Exception e) { 
	                Log.e("TCP", "S: Error", e);} 
	            
	            finally {
	                socket.close();}
	        } 
	        catch (Exception e) {
	        	// lets write some stuff that asks for the IP again. And say: error your mom is
	        	// not connected to the wifi or her IP address is wrong. Please re-enter her IP:
	             Log.e("TCP", "C: Error", e);}
        }
    }
  //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}