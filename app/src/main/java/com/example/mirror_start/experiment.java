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
import java.util.concurrent.atomic.AtomicLong;

public class experiment extends AppCompatActivity {


    public static final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels/1.5);

    Button start_animation, BackToMain;
    ArrayList<float[]> coordinates = new ArrayList<float[]>();
    ArrayList<float[]> coordinates_res = new ArrayList<float[]>();
    ImageView RedDot, GreenDot;
    AtomicBoolean actionDownFlag = new AtomicBoolean(true);
    AtomicLong x = new AtomicLong();
    AtomicLong y = new AtomicLong();
    long hold_counter = 1;
    boolean is_running = false;
    Sleeper sleeper = new Sleeper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation2);
        coordinates = (ArrayList<float[]>)getIntent().getSerializableExtra("exp_param");
        int numOfSamples = coordinates.size();
        GreenDot = findViewById(R.id.GreenDot);
        RedDot = findViewById(R.id.RedDot);

        // creating button to start animation with onClick listener
        start_animation = findViewById(R.id.start_animation);
        start_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ animate(RedDot, createPath(coordinates), numOfSamples);}
        });
        BackToMain = findViewById(R.id.return_to_main);
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
        path.lineTo(width*coordinates.get(0)[0], height*coordinates.get(0)[1]);
        for(int i=1; i<coordinates.size(); i++){
            if(coordinates.get(i)[0] == coordinates.get(i-1)[0]){
                hold_counter++;
                continue;
            }
            path.lineTo(width*coordinates.get(i)[0], height*coordinates.get(i)[1]);
        }
        return path;
    }

    /***
     * Start animation according to given Path
     * sets initial values for x,y coordinates
     * call sample Runnable and start sampling
     * call animation time thread that sets the sampling time
      */
    public void animate(View view, Path path, int numOfSamples) {
        long duration = (numOfSamples/60)* 1000L - (hold_counter/60)*1000;
        Log.d("duration=", String.valueOf(duration));
        Thread animation_time = new Thread(new animation_sleep(duration+(hold_counter/60)*1000));

        x.set((long)(width*coordinates.get(0)[0]));
        y.set((long)(height*coordinates.get(0)[1]));
        Thread sample = new Thread(new sample_runnable());

        ObjectAnimator animation = ObjectAnimator.ofFloat(view, View.X, View.Y, path);
        animation.setDuration(duration);
        animation.setStartDelay((hold_counter/60)*1000);
        Log.d("hold_counter=", String.valueOf(hold_counter));
        animation.start();

        sample.start();
        animation_time.start();

    }


    /**
     * Starts when users touch screen
     * Receives users touch and sample it in 60 HZ
     * Moving the green dot object according to X values sampled
     * Creating ArrayList structure of sampled coordinates that will be returned to the MainActivity*/
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        if (!is_running)
            return false;
        x.set((long)(event.getX()));
        y.set((long)(event.getY()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                GreenDot.setX(event.getX());
                GreenDot.setY((float) (RedDot.getY() * 1.2));
                break;
        }
        return false;
    }

    /**
     * This class implements the sample runnable that sample the coordinates in 60 Hz
     * we use sleepNanos method because it is more accurate
     * we subtract the UI loop time and the inaccuracy
     */
    public class sample_runnable implements Runnable {
        private final float[] coordinates_sample = new float[2];

        public void run() {
            try {
                long startTime, endTime, sleep_loss_avg = 0;
                startTime = System.nanoTime();
                while (actionDownFlag.get()){
                    coordinates_sample[0] = ((float)(x.get()))/width;
                    coordinates_sample[1] = ((float)(y.get()))/height;
                    add_coordinates(coordinates_res, coordinates_sample[0], coordinates_sample[1]);

                    Log.d("Tag", "X = " + coordinates_sample[0] + ", Y = " + coordinates_sample[1]);
//                    Log.d("exe time", String.valueOf(System.nanoTime() - startTime));
                    endTime = System.nanoTime();
                    sleeper.sleepNanos(1000000000/60 - (endTime - startTime) -sleep_loss_avg);
                    sleep_loss_avg = ((System.nanoTime() - endTime) - (1000000000/60- (endTime - startTime)));
                    startTime = System.nanoTime();

                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        public void add_coordinates(ArrayList<float[]> coordinates_res, float x, float y){
            float[] samples = new float[2];
            samples[0] = x;
            samples[1] = y;
            coordinates_res.add(samples);
        }
    }

    /**
     * This class implements Thread that controls the experiment time
     * when the animation is done touch event is disable
     */
    public class animation_sleep extends Thread{
        private final long duration;

        public animation_sleep(long duration) {
            this.duration = duration;
        }

        public void run(){
            try {
                is_running = true;
                sleeper.sleepNanos(duration*1000000);
                actionDownFlag.set(false);
                is_running = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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