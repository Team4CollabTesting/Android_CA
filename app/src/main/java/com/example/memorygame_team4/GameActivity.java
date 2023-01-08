package com.example.memorygame_team4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends AppCompatActivity {
    private ArrayList<String> images;
    private int totalSelectedImages;
    private int firstClickIndex;
    private int secondClickIndex;
    private String firstClick_fileName;
    private String secondClick_fileName;
    private int score;
    private TextView score_display;
    private ArrayList<Integer> openedImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
        setupImageListener();
    }

    private void setupImageListener() {
        for (int i=1; i <= 12; i++){
            Resources res = getResources();
            ImageView imageView = (ImageView) findViewById(res.getIdentifier("imgView" + i, "id", getPackageName()));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id_name = getResources().getResourceEntryName(v.getId());
                    int index = Integer.parseInt(id_name.substring(7));
                    String selected_filename = images.get(index-1);
                    if(totalSelectedImages == 0 && !openedImages.contains(index)){
//                        imageView.setBackgroundResource(R.drawable.border_image);
                        firstClickIndex = index;
                        firstClick_fileName = selected_filename;
                        updateImageView(selected_filename, firstClickIndex);
                        totalSelectedImages++;
                        openedImages.add(index);
                    } else if (totalSelectedImages == 1){
                        secondClickIndex = index;
                        secondClick_fileName = selected_filename;
                        updateImageView(selected_filename, secondClickIndex);
                        totalSelectedImages++;

                        if(selected_filename.equals(firstClick_fileName)){
                            updateScore();
                            launchMainActivity();
                            openedImages.add(index);
                            resetPointers();
                        } else {
                            wrong_attempt(imageView);
                        }
                    }
                }
            });
        }
    }

    public void updateImageView(String filename, int imgId){
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File fileDestination = new File(dir, filename);

        Bitmap bitmap = BitmapFactory.decodeFile(fileDestination.getAbsolutePath());
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 150, 150, true);

        Resources res = getResources();
        ImageView imageView = (ImageView) findViewById(res.getIdentifier("imgView" + imgId, "id", getPackageName()));
//        ImageView imageView1 = findViewById(R.id.imgView1);

        imageView.setImageBitmap(resized);
    }

    public void init(){
        images = (ArrayList<String>) getIntent().getSerializableExtra("chosenImages");
        totalSelectedImages = 0;
        firstClickIndex = 0;
        secondClickIndex = 0;
        firstClick_fileName = "";
        secondClick_fileName = "";
        openedImages = new ArrayList<>();
        score = 0;
        score_display = findViewById(R.id.score_display);
    }
    public void resetPointers(){
        firstClickIndex = 0;
        secondClickIndex = 0;
        firstClick_fileName= "";
        secondClick_fileName= "";
        totalSelectedImages=0;
    }
    public void updateScore(){
        if(score < 5){
            score++;
            score_display.setText(score+" of 6 matches");
        } else {
            score++;
            score_display.setText("Congratulations: "+score+" of 6 matches");
        }
    }
    public void launchMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        if(score == 6){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                }
            },3000);
        }
    }
    public void wrong_attempt(ImageView v){
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setImageResource(R.drawable.cross);
                Resources res = getResources();
                ImageView firstClickImage = findViewById(
                        res.getIdentifier("imgView"+firstClickIndex,"id",getPackageName()));
                firstClickImage.setImageResource(R.drawable.cross);
                openedImages.remove(Integer.valueOf(firstClickIndex));
                resetPointers();
            }
        }, 1500);
    }

}