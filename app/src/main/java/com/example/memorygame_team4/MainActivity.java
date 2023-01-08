package com.example.memorygame_team4;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> twentyImages;
    private ArrayList<String> twentyImageURLs;
    private ArrayList<String> sixImages;
    private Button fetch_btn;
    private EditText url_input;
    private Thread thread;
    private ProgressBar progressBar;
    private TextView progressMessage;
    private TextView score;

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("download_completed")) {
                String filename = intent.getStringExtra("filename");
                int count = intent.getIntExtra("count", 0);

                    updateImageView(filename, count);
                    updateProgress();
                    twentyImages.add(filename);
                if (count < twentyImageURLs.size()) {
                    count++;
                    startDownloadImage(twentyImageURLs.get(count-1), count);
                } else return;
                }
            }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        twentyImageURLs = new ArrayList<>();
        twentyImages = new ArrayList<>();
        sixImages = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        progressMessage = findViewById(R.id.progressMsg);

        setupFetchBtn();
        initReceiver();
        setupImageListener();

    }
    public void setupFetchBtn(){
        fetch_btn = findViewById(R.id.fetch_btn);
        fetch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (thread != null) {interruptThread();}
                url_input = findViewById(R.id.url);
                String url = url_input.getText().toString();
                validateURL(url);
                fetchImageURLs(url);

                try{
                    thread.join();}
                catch(InterruptedException e){
                    System.out.println("InterruptedException");}

                validateImageURL();
                startDownloadImage(twentyImageURLs.get(0), 1);
            }
        });
    }

    public void setupImageListener(){
        for(int i=1; i <= 20 ; i++){
            Resources res = getResources();
            ImageView imageView = (ImageView) findViewById(res.getIdentifier("imgView" + i, "id", getPackageName()));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String idName = getResources().getResourceEntryName(v.getId());
                    int index = Integer.parseInt(idName.substring(7));

                    if(!sixImages.contains(twentyImages.get(index-1))){
                        if(sixImages.size() < 6){
                            selectImage(imageView, index-1);
                        }
                    }else {
                        unselectImage(imageView, index-1);
                    }
                    if(sixImages.size() == 6){
                        launchGameActivity();
                    }
                }
            });
        }
    }

    protected void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("download_completed");

        registerReceiver(receiver, filter);
    }


    protected void startDownloadImage(String imgURL, int count) {
        Intent intent = new Intent(this, ImageDLService.class);
        intent.setAction("download_file");
        intent.putExtra("url", imgURL);
        intent.putExtra("count", count);

        startService(intent);
    }

    protected void updateImageView(String filename, int imgId) {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File fileDestination = new File(dir, filename);

        Bitmap bitmap = BitmapFactory.decodeFile(fileDestination.getAbsolutePath());
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 150, 150, true);

        Resources res = getResources();
        ImageView imageView = (ImageView) findViewById(res.getIdentifier("imgView" + imgId, "id", getPackageName()));
//        ImageView imageView1 = findViewById(R.id.imgView1);

        imageView.setImageBitmap(resized);
    }
    protected void updateProgress(){
        int curProgress =  progressBar.getProgress();
        if(curProgress != 95){
            progressBar.setProgress(curProgress + 5);
            progressMessage.setText("Downloading: "+ curProgress+ "%");
        } else {
            progressBar.setProgress(curProgress +5);
            progressMessage.setText("Download Complete ✅");
        }
    }

    protected void fetchImageURLs(String URL) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect(URL).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (doc != null) {
                    Elements el = doc.select("img");
                    int loop =0;
                    while (twentyImageURLs.size() < 20) {
                        String imgURL = el
                                .select("img")
                                .eq(loop)
                                .attr("src");
                        if (imgURL.endsWith(".png")
                                || imgURL.endsWith(".jpg")
                                || imgURL.endsWith(".jpeg")) {
                            twentyImageURLs.add(imgURL);
                        }
                        loop++;
//                            Log.d("ImageURLS", "ImageURLS: " + imgURL);
                    }
                }
            }
        });
        thread.start();
    }

    public void showReadyUI()
    {
        url_input.setText("");
        for(int i=1; i <= 20 ; i++){
            Resources res = getResources();
            ImageView imageView = (ImageView) findViewById(res.getIdentifier("imgView" + i, "id", getPackageName()));
            imageView.setImageResource(R.drawable.cross);
        }
        twentyImages.clear();
        twentyImageURLs.clear();
        progressMessage.setText("");
        progressBar.setProgress(0);
        sixImages.clear();

    }
    public void selectImage(ImageView img,int fileLocation){
        img.setBackgroundResource(R.drawable.border_image);
        sixImages.add(twentyImages.get(fileLocation));

    }
    public void unselectImage(ImageView img,int fileLocation){

        sixImages.remove(twentyImages.get(fileLocation));
        img.setBackgroundResource(0);
    }
    public void launchGameActivity(){
        Intent intent = new Intent(this, GameActivity.class);
        ArrayList<String> chosenImages = new ArrayList<>();
        chosenImages.addAll(sixImages);
        chosenImages.addAll(sixImages);
        Collections.shuffle(chosenImages);
        intent.putExtra("chosenImages", chosenImages);
        startActivity(intent);
    }

    public void validateURL(String url){
        if(!URLUtil.isValidUrl(url)){
            Toast.makeText(getApplicationContext(), "Invalid URL, please try again!", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    public void validateImageURL (){
        if(twentyImageURLs.size() < 20){
            Toast.makeText(getApplicationContext()
                    , "Current URL does not contain enough images to start the game, please choose a different URL"
                    , Toast.LENGTH_SHORT).show();
            return;
        }
    }
    public void interruptThread(){
        thread.interrupt();
        showReadyUI();
    }
}
