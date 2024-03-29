package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class experiment extends AppCompatActivity {


    public static final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels);

    Button start_animation, BackToMain;
    TextView anime_text;
    ArrayList<float[]> coordinates = new ArrayList<float[]>();
    ArrayList<float[]> coordinates_res = new ArrayList<float[]>();
    ImageView RedDot, GreenDot;
    AtomicBoolean actionDownFlag = new AtomicBoolean(true);
    AtomicLong x = new AtomicLong();
    AtomicLong y = new AtomicLong();
    long hold_counter = 1, duration;
    boolean is_running = false, is_touch = false;
    Sleeper sleeper = new Sleeper();
    long t=0;
    ObjectAnimator animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiment);
        coordinates = (ArrayList<float[]>)getIntent().getSerializableExtra("exp_param");
        int numOfSamples = coordinates.size();
        GreenDot = findViewById(R.id.GreenDot);
        RedDot = findViewById(R.id.RedDot);
        RedDot.setVisibility(View.INVISIBLE);
        GreenDot.setVisibility(View.INVISIBLE);
        anime_text = findViewById(R.id.textView);
        anime_text.setVisibility(View.INVISIBLE);
        // creating button to start animation with onClick listener
        start_animation = findViewById(R.id.start_animation);
        start_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                animate(RedDot, createPath(coordinates), numOfSamples);
                start_animation.setVisibility(View.INVISIBLE);
            }
        });
        BackToMain = findViewById(R.id.back);
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
        path.moveTo(width*coordinates.get(0)[0]-(float)(RedDot.getWidth())/2, (float)(RedDot.getY()));
        path.lineTo(width*coordinates.get(0)[0]-(float)(RedDot.getWidth())/2, (float)(RedDot.getY()));
        int i=1;
        while(coordinates.get(i)[0] == coordinates.get(i - 1)[0]) {
                hold_counter++;
                i++;
        }
        for (int j = (int)hold_counter; j<coordinates.size(); j++){
            path.lineTo(width*coordinates.get(j)[0]-(float)(RedDot.getWidth())/2, (float)(RedDot.getY()));
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
        duration = (numOfSamples / 60) * 1000L - (hold_counter / 60) * 1000;
        Log.d("duration=", String.valueOf(duration));
        anime_text.setVisibility(View.VISIBLE);
        is_touch = true;


        animation = ObjectAnimator.ofFloat(view, View.X, View.Y, path);
        animation.setDuration(duration);
        animation.setStartDelay((hold_counter / 60) * 1000);
        animation.setInterpolator(new LinearInterpolator());
        Log.d("hold_counter=", String.valueOf(hold_counter));
    }


    /**
     * Starts when users touch screen
     * Receives users touch and sample it in 60 HZ
     * Moving the green dot object according to X values sampled
     * Creating ArrayList structure of sampled coordinates that will be returned to the MainActivity*/
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Thread sample = new Thread(new sample_runnable());
        Thread animation_time = new Thread(new animation_sleep(duration + (hold_counter / 60) * 1000));


        if(!is_touch & !is_running)
            return false;
        if (is_touch & !is_running) {
            RedDot.setVisibility(View.VISIBLE);
            GreenDot.setVisibility(View.VISIBLE);
            anime_text.setVisibility(View.INVISIBLE);
            x.set((long)(event.getX()));
            y.set((long)(event.getY()));
            animation.start();
            sample.start();
            animation_time.start();
            is_touch=false;
            return  false;
        }

        x.set((long)(event.getX()));
        y.set((long)(event.getY()));

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                GreenDot.setX(event.getX()-(float)(GreenDot.getWidth())/2);
                GreenDot.setY((float) (RedDot.getY() * 1.2));
                break;

            default:
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
                long startTime, endTime, sleep_loss_avg = 0, timestamp, time_reference;
                time_reference = System.nanoTime();
                startTime = time_reference;
                while (actionDownFlag.get()){
                    coordinates_sample[0] = ((float)(x.get()))/width;
                    coordinates_sample[1] = ((float)(y.get()))/height;
                    timestamp = System.nanoTime() - time_reference;
                    add_coordinates(coordinates_res, coordinates_sample[0], coordinates_sample[1], timestamp);

                    endTime = System.nanoTime();
                    sleeper.sleepNanos(1000000000/60 - (endTime - startTime) -sleep_loss_avg);
                    sleep_loss_avg = ((System.nanoTime() - endTime) - (1000000000/60- (endTime - startTime)));
                    startTime = System.nanoTime();

                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        public void add_coordinates(ArrayList<float[]> coordinates_res, float x, float y, long t){
            float[] samples = new float[3];
            samples[0] = x;
            samples[1] = y;
            samples[2] = (float)(t);
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
                anime_text.setVisibility(View.INVISIBLE);
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