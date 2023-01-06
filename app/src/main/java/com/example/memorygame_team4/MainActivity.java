package com.example.memorygame_team4;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> twentyImages;
    private ArrayList<String> twentyImageURLs;
    private Button fetch_btn;
    private EditText url_input;
    private String imgURLTest;
    private Thread thread;

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("download_completed")) {
                String filename = intent.getStringExtra("filename");
                int imgViewId = intent.getIntExtra("count", 0);
                if(imgViewId <= 20){
                    updateImageView(filename, imgViewId);
                } else return;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetch_btn = findViewById(R.id.fetch_btn);
        fetch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url_input = findViewById(R.id.url);
                String url = url_input.getText().toString();
                fetchImageURLs("https://stocksnap.io/");
                int count = getIntent().getIntExtra("count", 0);
                for(String imgURL : twentyImageURLs){
                    if(count != 20){
                        count++;
                        startDownloadImage(imgURL, count);
                    } else return;

                }
            }
        });
        initReceiver();
    }
    protected void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("download_completed");

        registerReceiver(receiver, filter);
    }

    public void setupImgListener(){

    }
    protected void startDownloadImage(String imgURL, int count) {
        Intent intent = new Intent(this, ImageDLService.class);
        intent.setAction("download_file");
        intent.putExtra("url", imgURL);
        intent.putExtra("count", count);

        startService(intent);
    }

    protected void updateImageView(String filename, int imgId){
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File fileDestination = new File(dir, filename);

        Bitmap bitmap = BitmapFactory.decodeFile(fileDestination.getAbsolutePath());

        Resources res = getResources();
        ImageView imageView = (ImageView)findViewById(res.getIdentifier("imgView"+ imgId, "id", getPackageName()));
//        ImageView imageView1 = findViewById(R.id.imgView1);

        imageView.setImageBitmap(bitmap);
    }

    protected void fetchImageURLs(String URL){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    twentyImageURLs = new ArrayList<>();
                    Document doc = Jsoup.connect(URL).get();

                    if(doc != null){
                        Elements el = doc.select("img");

                        for(int i=0; i <= 20; i++){
                            String imgURL = el
//                                    .select("img")
                                    .eq(i)
                                    .attr("src");
                            if(imgURL.endsWith(".png")
                                    || imgURL.endsWith(".jpg")
                                    || imgURL.endsWith(".jpeg")){
                                twentyImageURLs.add(imgURL);
                            }
                            Log.d("ImageURLS", "ImageURLS: "+ imgURL);
                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();


    }
}