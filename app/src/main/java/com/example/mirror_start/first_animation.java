package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class first_animation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_animation);
        ImageView view = (ImageView) findViewById(R.id.object);
        linear_movement(view);

    }

    public void BackToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void linear_movement(View view){
        Animation lin_mov = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.line_animation);
        view.startAnimation(lin_mov);
    }
}