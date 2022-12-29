package com.example.mirror_start;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Instructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        FloatingActionButton back = (FloatingActionButton) findViewById(R.id.floatingActionButton);

    }

    public void BackToMainFunc(View view){
        finish();
    }
}

//need to add go back to main screen function