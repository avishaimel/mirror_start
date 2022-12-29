package com.example.mirror_start;

import static android.app.PendingIntent.getActivity;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DrawFilter;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static final String SERVER_IP = "192.168.1.116"; //server IP address
    public static final int SERVER_PORT = 1237;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private InputStream inputStream = null;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
//                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;
        MainActivity.getInstance().setConnectionStatus(false);

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }



    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            MainActivity.getInstance().setConnectionStatus(true);

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                inputStream = socket.getInputStream();

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(inputStream));
                //in this while the client listens for the messages sent by the server
                String fileName = null;
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage == null) continue;

                    if(mServerMessage.equals("START")){
                        fileName = "location.png";
                    }
                    if (mServerMessage.equals("MIDDLE")){
                        fileName = "speeds.png";
                    }

                    if(mServerMessage.contains("valid")){
                        try {
                            sendMessage("start");
//                            receiveFile(MainActivity.getInstance().getFilePath(), Integer.parseInt(mServerMessage.replaceAll("[^0-9]", "")));
                            int fileSize = Integer.parseInt(mServerMessage.replaceAll("[^0-9]", ""));
                            String finalName = MainActivity.getInstance().getFilePath(fileName);
                            byte[] data = new byte[8 * 1024];
                            int bToRead;
                            FileOutputStream fos = new FileOutputStream(finalName);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);

                            // make sure not to read more bytes than filesize
                            if (fileSize > data.length) bToRead = data.length;
                            else bToRead = fileSize;
                            Thread.sleep(200);
                            while (true) {
                                Thread.sleep(100);
                                int bytesRead = inputStream.read(data, 0, bToRead);
                                if (bytesRead < bToRead){
                                    if(bytesRead>0) {
                                        bos.write(data, 0, bytesRead);
                                    }
                                    break;
                                }
                                bos.write(data, 0, bytesRead);
                                if (bytesRead == fileSize)
                                    break;
                            }
                            bos.close();
//                            String a = mBufferIn.readLine();
                            sendMessage("done");
                            Thread.sleep(1);
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);

                        }
                }

                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                MainActivity.getInstance().setConnectionStatus(false);
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);

        }

    }


//    private void saveImage(Bitmap fiknalBitmap){
//
//        String root = Environment.getExternalStorageDirectory().toString();
//        File f = new File(root + "/image");
//        f.mkdirs();
//        Random generator = new Random();
//        int n = 1000;
//        n = generator.nextInt(n);
//        String fname = "Image-" + n + ".png";
//        File file = new File(f,fname);
//    }



    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }







}