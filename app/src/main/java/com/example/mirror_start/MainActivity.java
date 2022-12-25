package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button send, connect, close_connection, experiment, displayResults;
    EditText e1;
    TcpClient mTcpClient;
    public static final String EXTRA = "exp_param";
    ArrayList<float[]> coordinates = new ArrayList<float[]>();
    int mat_index = 0;
    private static final int EXP_ACTIVITY_REQUEST_CODE = 0;
    boolean is_png = false;
    private static MainActivity instance;
    Bitmap bmp = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        /** These are the TCP client relevant buttons */
        send =(Button) findViewById(R.id.send_button);
        connect =(Button) findViewById(R.id.connect_button);
        close_connection =(Button) findViewById(R.id.close_button);
        e1 = (EditText) findViewById(R.id.editText);

        /** changing to experiment activity */
        experiment =(Button) findViewById(R.id.StartExperiment);
        experiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openExperiment();}
        });
        displayResults =(Button) findViewById(R.id.displayResults);
        displayResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openResults();}
        });

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {send_message(e1.getText().toString());}
        });
    }

    /**
     * Receiving the experiment result coordinates */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXP_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<float[]> coordinates_res  = (ArrayList<float[]>)data.getSerializableExtra("exp_res");
                SendResults(coordinates_res);
                Log.d("worked:", String.valueOf(coordinates_res.size()));
                coordinates_res.clear();
                coordinates.clear();
//                close_connection_no_button();
            }

        }
    }

    public void openExperiment() {

            Intent intent = new Intent(this, experiment.class);
            intent.putExtra(EXTRA, coordinates);
            startActivityForResult(intent, EXP_ACTIVITY_REQUEST_CODE);

    }

    public void openResults() {

        Intent intent = new Intent(this, DisplayResults.class);
        startActivity(intent);

    }


    public void connect_server(View view){
        //Connet to server
        new ConnectTask().execute("");
    }

    public void send_message(String str) {
        if (mTcpClient != null) {
            mTcpClient.sendMessage(str);
        }
    }

    public void close_connection(View view) {
        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
    }


    //For each csv line -  convert to float x/y values and add to coordinates List
    public void  AddCoordinates(ArrayList<float[]> coordinates, String values){
        String[] row = values.split(",");
        float[] row_val = new float[2];
        row_val[0] = Float.parseFloat(row[0]);
        row_val[1] = Float.parseFloat(row[1]);
        coordinates.add(row_val);
    }

    public void SendResults(ArrayList<float[]> coordinates_res){
        String sample_to_send ="";
        float x,y,t;
        Thread touch = new Thread();
        try {
            for(int i = 0; i<coordinates_res.size(); i++){
            x = coordinates_res.get(i)[0];
            y = coordinates_res.get(i)[1];
            t = coordinates_res.get(i)[2];
            sample_to_send = sample_to_send + x + "," + y + "," + t + "\n";
            touch.sleep(2);
        }
            sample_to_send = sample_to_send +"end";
            send_message(sample_to_send);
        }catch (Exception e){}

    }

    public String getFilePath(String filename){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File imageDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(imageDirectory, filename);
        return file.getPath();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

            if(values[0].equals("START")){
                is_png = true;
            }
            else if (values[0].equals("END")){
                is_png = false;
            }
            else if(!(values[0].isEmpty()) & !is_png) {
                AddCoordinates(coordinates, values[0]);
            }

        }
    }

}