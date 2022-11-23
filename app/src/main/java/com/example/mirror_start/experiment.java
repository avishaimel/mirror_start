package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class experiment extends AppCompatActivity {


    public static final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels/1.5);

    Button start_animation, BackToMain;
    ArrayList<float[]> coordinates_res = new ArrayList<float[]>();
    ImageView RedDot;
    AtomicBoolean actionDownFlag = new AtomicBoolean(true);
    float x, y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation2);
        RedDot = (ImageView) findViewById(R.id.RedDot);

        ArrayList<float[]> coordinates = (ArrayList<float[]>)getIntent().getSerializableExtra("exp_param");
        int numOfSamples = coordinates.size();

        Log.d("val is - ", Float.toString(coordinates.get(13)[0]) + "," +Float.toString(coordinates.get(13)[1]));
        // creating button to start animation with onClick listener
        start_animation = (Button) findViewById(R.id.start_animation);
        start_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ animate(RedDot, createPath(coordinates), numOfSamples);}
        });
        BackToMain = (Button) findViewById(R.id.return_to_main);
        BackToMain.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view){ BackToMainFunc(view);}
        }));
    }

    /**
     *  creates and return path for animation
     *  can add different Path method to control the path*/
    public Path createPath(ArrayList<float[]> coordinates) {
        Path path = new Path();
        path.moveTo(width*coordinates.get(0)[0], height*coordinates.get(0)[1]);
        for(int i=0; i<coordinates.size(); i++){
            path.lineTo(width*coordinates.get(i)[0], height*coordinates.get(i)[1]);
        }
        return path;
    }

    // Start animation according to given Path
    public void animate(View view, Path path, int numOfSamples) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, View.X, View.Y, path);
        animation.setDuration((numOfSamples/60)*1000);
        animation.start();
    }


    /**
     * Starts when users touch screen
     * Receives users touch and sample it in 60 HZ
     * Moving the green dot object according to X values sampled
     * Creating ArrayList structure of sampled coordinates that will be returned to the MainActivity*/
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        ImageView GreenDot = findViewById(R.id.GreenDot);
        float[] coordinates_sample = new float[2];
        x = event.getX();
        y = event.getY();
        Thread t = new Thread(new MyRunnable(x,y));
        actionDownFlag.set(false);
        try {
            Thread.sleep(1000/60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        float xDown = 0, yDown = 0, moovedX = 0, moovedY = 0 , distanceX, distanceY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDownFlag.set(true);
                break;

            case MotionEvent.ACTION_MOVE:
                actionDownFlag.set(true);
                GreenDot.setX(x);
                GreenDot.setY((int) (RedDot.getY()*1.2));
                break;

            case MotionEvent.ACTION_UP:
                actionDownFlag.set(false);
                return true;
        }
        t.start();
        return true;
    }

    public class MyRunnable implements Runnable {
        private float x, y;
        private float[] coordinates_sample = new float[2];

        public MyRunnable(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void run() {
            try {
                while (actionDownFlag.get()){
                    coordinates_sample[0] = x;
                    coordinates_sample[1] = y;
                    coordinates_res.add(coordinates_sample);
                    Log.d("Tag", "X = " + String.valueOf(coordinates_sample[0]) + ", Y = " + String.valueOf(coordinates_sample[1]));
                    Thread.sleep(1000 / 60);// sample in 60 HZ
                }
            } catch (Exception e) {}
        }
    }




    /**
     * On click function - return to MainActivity
     * sends results coordinates as ArrayList*/
    public void BackToMainFunc(View view){
        Intent intent = new Intent();
        intent.putExtra("exp_res", coordinates_res);
        setResult(RESULT_OK, intent);
        finish();
    }


}