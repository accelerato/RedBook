package com.example.redbook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewPagerSampleActivity extends FragmentActivity {

    static final int PAGE_COUNT = 231;
    final int MAX_PAGE_LIMIT = 10;

    InfoItemArray infoItemArray = new InfoItemArray();

    ViewPager pager;
    PagerAdapter pagerAdapter;
    TextView titleName;
    int lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pager = (ViewPager) findViewById(R.id.pager);
//        pager.setOffscreenPageLimit(10);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        titleName = findViewById(R.id.name_cat_txt);
        titleName.setText(Html.fromHtml("<big>Млекопитающие животные</big>"));

        titleName.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/font_2.ttf"));

        pager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d("myLogs", "onPageSelected, position = " + position);


                if (position > lastPosition) {

                    if (position >= MAX_PAGE_LIMIT && (infoItemArray.get(position - MAX_PAGE_LIMIT).getImageItem() != null)) {
                        Log.d("myLogs", "> delete position = " + (position - MAX_PAGE_LIMIT));
                        infoItemArray.get(position - MAX_PAGE_LIMIT).recycle();
                        infoItemArray.get(position - MAX_PAGE_LIMIT).setImageItem(null);
                    }

                    if ((lastPosition + MAX_PAGE_LIMIT) < PAGE_COUNT && infoItemArray.get(lastPosition + MAX_PAGE_LIMIT).getImageItem() == null){
                        Log.d("myLogs", "> add position = " + (lastPosition + MAX_PAGE_LIMIT));
                        load(lastPosition + MAX_PAGE_LIMIT);
                    }

                } else {

                    if ((position + MAX_PAGE_LIMIT + 1) < PAGE_COUNT && infoItemArray.get(position + MAX_PAGE_LIMIT + 1).getImageItem() != null) {
                        Log.d("myLogs", "< delete position = " + (position + MAX_PAGE_LIMIT + 1));
                        infoItemArray.get(position + MAX_PAGE_LIMIT + 1).recycle();
                        infoItemArray.get(position + MAX_PAGE_LIMIT + 1).setImageItem(null);
                    }

                    if (lastPosition >= MAX_PAGE_LIMIT && (infoItemArray.get(lastPosition - MAX_PAGE_LIMIT).getImageItem() == null)){
                        Log.d("myLogs", "< add position = " + (lastPosition - MAX_PAGE_LIMIT));
                        load(lastPosition - MAX_PAGE_LIMIT);
                    }

                }

                lastPosition = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
//                Log.d("myLogs", "onPageScrolled, position = " + position +
//                        ", positionOffset = " + positionOffset + ", positionOffsetPixels = " + positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                Log.d("myLogs", "onPageScrollStateChanged, state = " + state);
            }
        });
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            loadListNames(infoItemArray);
            infoItemArray.shuffle();
            loadTextAndImage(0, PAGE_COUNT, 3, infoItemArray);
            loadTextAndImage(1, PAGE_COUNT, 3, infoItemArray);
            loadTextAndImage(2, PAGE_COUNT, 3, infoItemArray);
//            Log.d("myLogs", "MyFragmentPagerAdapter");
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("myLogs", "getItem, position = " + position);

            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
//            Log.d("myLogs", "getCount");
            return PAGE_COUNT;
        }

    }

    public void loadListNames(final InfoItemArray infoItemArray) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                String wikipeadiaApiJson;
                String responseSB;
                HttpURLConnection httpcon;
                BufferedReader in;
                String encoding = "UTF-8";


                try {
                    wikipeadiaApiJson = "https://ru.wikipedia.org/w/api.php?action=parse&format=json&utf8=1&page=" + URLEncoder.encode("Список угрожаемых видов млекопитающих", encoding).replace("+", "%20");

                    httpcon = (HttpURLConnection) new URL(wikipeadiaApiJson).openConnection();
                    httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");

                    in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
                    responseSB = in.readLine();
                    in.close();

                    Pattern p = Pattern.compile("([^)]\\\\\">)(\\D+)(<\\/a><\\/dd>)");
                    Matcher m = p.matcher(responseSB);


                    while (m.find())
                        infoItemArray.add(m.group(2));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();

        while (t.isAlive())
            SystemClock.sleep(1);

    }

    void load(final int index){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String wikipeadiaApiJson;
                    String responseSB;
                    HttpURLConnection httpcon;
                    BufferedReader in;

                    String encoding = "UTF-8";
                    try {

                        boolean b = true;
//                                String randomValue = listNames.get((int) (Math.random() * listNames.size()));
                        String randomValue = infoItemArray.get(index).getNameItem();
                        Log.d("myLogs", "loadTextAndImage, position = " + index + ", name = " + randomValue);

                        wikipeadiaApiJson = "https://ru.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&redirects=1&utf8=1&explaintext=1&titles="
                                + URLEncoder.encode(randomValue, encoding);


                        httpcon = (HttpURLConnection) new URL(wikipeadiaApiJson).openConnection();
                        httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                        in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
                        responseSB = in.readLine();
                        in.close();


                        Pattern p2 = Pattern.compile("((extract)\":\"(.+?)\\\\n\\\\n\\\\n)|(== (.+?)==\\\\n(.+?)\\\\n\\\\n)");
                        Matcher m2 = p2.matcher(responseSB);

                        String text = "<br>";
                        while (m2.find()) {
                            if (m2.group(2) != null && m2.group(2).equals("extract"))
                                text += "<big><big><b><font color=#FFFFFF>Основная информация</font></b></big></big><br>" + m2.group(3);
                            else {
                                if (m2.group(6).charAt(0) == '\\')
                                    text += "<big><big><b><font color=#FFFFFF>" + m2.group(5).replace("=", "").replace("\n", "").trim() + "</font></b></big></big><br>" + m2.group(6).substring(2);
                                else
                                    text += "<big><big><b><font color=#FFFFFF>" + m2.group(5).replace("=", "").replace("\n", "").trim() + "</font></b></big></big><br>" + m2.group(6);
                            }
                            text += "<br><br><br>";
                            b = false;
                        }
                        if (b) {
                            Log.d("myLogs", "Информации не найдено - " + infoItemArray.get(index).getNameItem());
                            text = "Информации не найдено";
                        }
                        infoItemArray.get(index).setInfoItem(text.replace("\\n", "\n"));

                        String URL_str = "https://raw.githubusercontent.com/accelerato/RedBook/master/images/" + URLEncoder.encode(randomValue, encoding).replace("+", "%20") + ".jpg";
                        infoItemArray.get(index).setImageItem(getBitmapFromURL(URL_str));


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


    public void loadTextAndImage(final int start, final int max, final int step, final InfoItemArray infoItemArray) {

        if (infoItemArray == null) {
            Log.d("myLogs", "listNames == null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                int index = start;
                while (index < max) {

                    while ((lastPosition + MAX_PAGE_LIMIT + 1) < index)
                        SystemClock.sleep(100);

                    try {
                        String wikipeadiaApiJson;
                        String responseSB;
                        HttpURLConnection httpcon;
                        BufferedReader in;

                        String encoding = "UTF-8";
                        try {

                            boolean b = true;
//                                String randomValue = listNames.get((int) (Math.random() * listNames.size()));
                            String randomValue = infoItemArray.get(index).getNameItem();
                            Log.d("myLogs", "loadTextAndImage, position = " + index + ", name = " + randomValue);

                            wikipeadiaApiJson = "https://ru.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&redirects=1&utf8=1&explaintext=1&titles="
                                    + URLEncoder.encode(randomValue, encoding);


                            httpcon = (HttpURLConnection) new URL(wikipeadiaApiJson).openConnection();
                            httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                            in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
                            responseSB = in.readLine();
                            in.close();


                            Pattern p2 = Pattern.compile("((extract)\":\"(.+?)\\\\n\\\\n\\\\n)|(== (.+?)==\\\\n(.+?)\\\\n\\\\n)");
                            Matcher m2 = p2.matcher(responseSB);

                            String text = "<br>";
                            while (m2.find()) {
                                if (m2.group(2) != null && m2.group(2).equals("extract"))
                                    text += "<big><big><b><font color=#FFFFFF>Основная информация</font></b></big></big><br>" + m2.group(3);
                                else {
                                    if (m2.group(6).charAt(0) == '\\')
                                        text += "<big><big><b><font color=#FFFFFF>" + m2.group(5).replace("=", "").replace("\n", "").trim() + "</font></b></big></big><br>" + m2.group(6).substring(2);
                                    else
                                        text += "<big><big><b><font color=#FFFFFF>" + m2.group(5).replace("=", "").replace("\n", "").trim() + "</font></b></big></big><br>" + m2.group(6);
                                }
                                text += "<br><br><br>";
                                b = false;
                            }
                            if (b) {
                                Log.d("myLogs", "Информации не найдено - " + infoItemArray.get(index).getNameItem());
                                text = "Информации не найдено";
                            }
                            infoItemArray.get(index).setInfoItem(text.replace("\\n", "\n"));

                            String URL_str = "https://raw.githubusercontent.com/accelerato/RedBook/master/images/" + URLEncoder.encode(randomValue, encoding).replace("+", "%20") + ".jpg";
                            infoItemArray.get(index).setImageItem(getBitmapFromURL(URL_str));


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }

                    index += step;
                }
            }
        }).start();
    }


    public Bitmap getBitmapFromURL(String src) {

        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
            connection.connect();

            InputStream in = connection.getInputStream(); //Ваш InputStream
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int origWidth = o.outWidth; //исходная ширина
            int origHeight = o.outHeight; //исходная высота
            int bytesPerPixel = 2; //соответствует RGB_555 конфигурации
            int maxSize = 480 * 800 * bytesPerPixel; //Максимально разрешенный размер Bitmap
            int desiredWidth = 480; //Нужная ширина
            int desiredHeight = 800; //Нужная высота
            int desiredSize = desiredWidth * desiredHeight * bytesPerPixel; //Максимально разрешенный размер Bitmap для заданных width х height
            if (desiredSize < maxSize) maxSize = desiredSize;
            int scale = 1; //кратность уменьшения
            int origSize = origWidth * origHeight * bytesPerPixel; //высчитываем кратность уменьшения
            if (origWidth > origHeight) {
                scale = Math.round((float) origHeight / (float) desiredHeight);
            } else {
                scale = Math.round((float) origWidth / (float) desiredWidth);
            }

            o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            o.inPreferredConfig = Bitmap.Config.RGB_565;

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            in = connection.getInputStream(); //Ваш InputStream. Важно - открыть его нужно еще раз, т.к второй раз читать из одного и того же InputStream не разрешается (Проверено на ByteArrayInputStream и FileInputStream).
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, o); //Полученный Bitmap

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}