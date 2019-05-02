package com.example.redbook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    Bitmap mIcon11 = null;

    ArrayList<String> arr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        imageView = (ImageView) findViewById(R.id.imageView);
        new CreatePage().execute();
    }


    public void createPage(View view){
        textView.setText(null);
        new CreatePage().execute();
    }

    public class CreatePage extends AsyncTask<Void,String,Void>{

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0].equals("URL"))
                imageView.setImageBitmap(mIcon11);
            else if (values[0].equals("Null"))
                textView.setText(null);
            else
                textView.append(values[0] + "\n\n\n");
        }

        @Override
        protected Void doInBackground(Void... voids) {



            String encoding = "UTF-8";

            try {
                String wikipeadiaApiJson;
                String responseSB = null;
                HttpURLConnection httpcon;
                BufferedReader in;
                ArrayList<String> values = null;
                if (arr == null) {
                    wikipeadiaApiJson = "https://ru.wikipedia.org/w/api.php?action=parse&format=json&page=Список%20угрожаемых%20видов%20млекопитающих&utf8=1";

                    httpcon = (HttpURLConnection) new URL(wikipeadiaApiJson).openConnection();
                    httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                    in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

                    responseSB = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        responseSB = in.lines().collect(Collectors.joining());
                    }
                    in.close();

                    Pattern p = Pattern.compile("(>)(\\D+)(<\\/a><\\/dd>)");
                    Matcher m = p.matcher(responseSB);

                    values = new ArrayList<String>();
                    while (m.find()) {
                        String value = m.group(2);
                        values.add(value);
                    }

                    arr = values;
                }
                boolean b = true;
                while (b) {
                    String randomValue = arr.get((int) (Math.random() * arr.size()));


                    wikipeadiaApiJson = "https://ru.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&explaintext=&utf8=1&titles="
                            + URLEncoder.encode(randomValue, encoding);


                    httpcon = (HttpURLConnection) new URL(wikipeadiaApiJson).openConnection();
                    httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                    in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        responseSB = in.lines().collect(Collectors.joining());
                    }
                    in.close();


                    Pattern p2 = Pattern.compile("((Описание ==\\\\n)|(Описание вида ==\\\\n)|(Распространение ==\\\\n)|(extract\":\"))(.+?)(\\\\n\\\\n\\\\n)");
                    Matcher m2 = p2.matcher(responseSB);

                    publishProgress(randomValue);
                    while(m2.find()) {
                        String result = m2.group(6);
                        publishProgress(result);
                        b = false;
                    }
                    if (b) {
                        publishProgress("Null");
                        continue;
                    }

                    wikipeadiaApiJson = "https://ru.wikipedia.org/w/api.php?action=parse&format=json&utf8=1&page="
                            + URLEncoder.encode(randomValue, encoding);


                    httpcon = (HttpURLConnection) new URL(wikipeadiaApiJson).openConnection();
                    httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                    in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        responseSB = in.lines().collect(Collectors.joining());
                    }
                    in.close();


                    Pattern p3 = Pattern.compile("(src=\\\\\"\\/\\/)(upload.+?)(\\\\\" decoding)");
                    Matcher m3 = p3.matcher(responseSB);


                    if (m3.find()) {
                        String URL_str = "https://" + m3.group(2);

                        URL newurl = null;
                        try {
                            newurl = new URL(URL_str);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        try {

                            mIcon11 = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        publishProgress("URL");
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
