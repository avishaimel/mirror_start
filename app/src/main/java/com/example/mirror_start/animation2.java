package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class animation2 extends AppCompatActivity {

    Button start_animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation2);
        ImageView dot = (ImageView) findViewById(R.id.object);

        // creating button to start animation with onClick listener
        start_animation = (Button) findViewById(R.id.start_animation);
        start_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ animate(dot, createPath());}
        });
    }

    // creates and return path for animation
    // can add different Path method to control the path
    public Path createPath() {
        Path path = new Path();
        path.arcTo(500f, 800f, 1000f, 1000f, -180f, 145f, true);
        return path;
    }

    // Start animation according to given Path
    public void animate(View view, Path path) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, View.X, View.Y, path);
        animation.setDuration(2000);
        animation.start();
    }

    public void BackToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}