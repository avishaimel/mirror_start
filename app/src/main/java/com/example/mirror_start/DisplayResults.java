package com.example.mirror_start;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DisplayResults  extends AppCompatActivity {

    Bitmap speeds, location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_results);
        ImageView speedsImage = (ImageView) findViewById(R.id.speed);
        ImageView locationImage = (ImageView) findViewById(R.id.location);

        speeds = setBitmap("speeds.png");
        location = setBitmap("location.png");


        speedsImage.setImageBitmap(speeds);
        locationImage.setImageBitmap(location);

        FloatingActionButton BackToMain = findViewById(R.id.return_to_main);
        BackToMain.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view){ BackToMainFunc(view);}
        }));

    }

    // Convert the received image to a Bitmap
    // If you do not want to return a bitmap comment/delete the folowing lines
    // and make the function to return void or whatever you prefer.
    private Bitmap setBitmap(String filename){
        Bitmap bmp = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(MainActivity.getInstance().getFilePath(filename));
            bmp = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }

    /**
     * On click function - return to MainActivity
     * sends results coordinates as ArrayList*/
    public void BackToMainFunc(View view){
        finish();
    }


}