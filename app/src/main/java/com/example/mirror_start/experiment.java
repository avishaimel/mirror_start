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

public class experiment extends AppCompatActivity {


    public static final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static final int height = Resources.getSystem().getDisplayMetrics().heightPixels;

    Button start_animation, BackToMain;
    ArrayList<float[]> coordinates_res = new ArrayList<float[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation2);
        ImageView dot = (ImageView) findViewById(R.id.object);

        ArrayList<float[]> coordinates = (ArrayList<float[]>)getIntent().getSerializableExtra("exp_param");
        int numOfSamples = coordinates.size();

        Log.d("val is - ", Float.toString(coordinates.get(13)[0]) + "," +Float.toString(coordinates.get(13)[1]));
        // creating button to start animation with onClick listener
        start_animation = (Button) findViewById(R.id.start_animation);
        start_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ animate(dot, createPath(coordinates), numOfSamples);}
        });
        BackToMain = (Button) findViewById(R.id.return_to_main);
        BackToMain.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view){ BackToMainFunc(view);}
        }));
    }

    // creates and return path for animation
    // can add different Path method to control the path
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


    // Receives users touch and sample it in 60 HZ
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float[] coordinates_sample = new float[2];
        Thread touch = new Thread();
        try {
            touch.sleep(1000/60);// sample in 60 HZ
            coordinates_sample[0]= event.getX() ;
            coordinates_sample[1] = event.getY();
            coordinates_res.add(coordinates_sample);
            Log.d("Tag", "X = " + String.valueOf(coordinates_sample[0]) + ", Y = " + String.valueOf(coordinates_sample[1]));
        }catch (Exception e){}

//        String s = String.valueOf(x);
//        Log.d("Tag", s);
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_MOVE:
//            case MotionEvent.ACTION_UP:
//
//        }

        return false;
    }

    public void BackToMainFunc(View view){
        Intent intent = new Intent();
        intent.putExtra("exp_res", coordinates_res);
        setResult(RESULT_OK, intent);
        finish();
    }


}