package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    Button send, connect, close_connection, animation_1, animation_2;
    EditText e1;
    TcpClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        e1 = (EditText) findViewById(R.id.editText);

        // These are the TCP client relevant buttons
        send =(Button) findViewById(R.id.send_button);
        connect =(Button) findViewById(R.id.connect_button);
        close_connection =(Button) findViewById(R.id.close_button);

        // changing to animation 1 activity
        animation_1 =(Button) findViewById(R.id.animation1);
        animation_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnimation(1);
            }
        });
        // changing to animation 2 activity
        animation_2 =(Button) findViewById(R.id.animation2);
        animation_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnimation(2);
            }
        });

    }

    public void openAnimation(int val) {
        if (val == 1) {
            Intent intent = new Intent(this, first_animation.class);
            startActivity(intent);
        }
        else if(val == 2) {
            Intent intent = new Intent(this, animation2.class);
            startActivity(intent);
        }
    }


    public void connect_server(View view){
        //Connet to server
        new ConnectTask().execute("");
    }

    public void send_message(View view) {
        if (mTcpClient != null) {
            mTcpClient.sendMessage(e1.getText().toString());
        }
    }

    public void close_connection(View view) {
        if (mTcpClient != null) {
            mTcpClient.stopClient();
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

        }
    }

}