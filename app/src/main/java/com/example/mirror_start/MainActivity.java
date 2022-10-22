package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {

    Button send, connect, close_connection, animation, sample;
    EditText e1;
    TcpClient mTcpClient;
    public static final String EXTRA = "exp_param";
    ArrayList<float[]> coordinates = new ArrayList<float[]>();
    int mat_index = 0;
    private static final int EXP_ACTIVITY_REQUEST_CODE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // These are the TCP client relevant buttons
        send =(Button) findViewById(R.id.send_button);
        connect =(Button) findViewById(R.id.connect_button);
        close_connection =(Button) findViewById(R.id.close_button);
        e1 = (EditText) findViewById(R.id.editText);

        // changing to animation 2 activity
        animation =(Button) findViewById(R.id.animation);
        animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openExperiment();}
        });
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {send_message(e1.getText().toString());}
        });

        try{
            ArrayList<float[]> coordinates_res = (ArrayList<float[]>)getIntent().getSerializableExtra("exp_res");
        }catch (Exception e){ Log.d("Err", "no results yet");}

    }

    public void openExperiment() {

            Intent intent = new Intent(this, experiment.class);
            intent.putExtra(EXTRA, coordinates);
            startActivityForResult(intent, EXP_ACTIVITY_REQUEST_CODE);

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

    public void send_results(ArrayList<float[]> coordinates_res){
        String sample_to_send;
        float x,y;
        for(int i = 0; i<coordinates_res.size(); i++){
            x = coordinates_res.get(i)[0];
            y = coordinates_res.get(i)[1];
            sample_to_send = String.valueOf(x) + String.valueOf(y);
            send_message(sample_to_send);
        }
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

            if(!(values[0].isEmpty())) {AddCoordinates(coordinates, values[0]);}

        }
    }

}