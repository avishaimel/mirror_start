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
        linear_movement();
        //BackToMain();

    }

    public void BackToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void linear_movement(){
        ImageView  object = findViewById(R.id.object);
        Animation lin_mov = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.line_animation);
        object.startAnimation(lin_mov);
    }
}