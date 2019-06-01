package com.example.redbook;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

public class ViewPagerSampleActivity extends AppCompatActivity {

    static int PAGE_COUNT = 231;
    final int MAX_PAGE_LIMIT = 10;

    InfoItemArray infoItemArray = new InfoItemArray();

    InfoItemArray favouriteArray = new InfoItemArray();
    InfoItemArray mainArray = new InfoItemArray();

    ViewPager pager;
    PagerAdapter pagerAdapter;
    TextView titleName;
    int lastPosition = 0;
    DrawerLayout drawerLayout;
    Handler handler = new Handler();
    int mainLastPosition = 0;
    NavigationView navigationView;

    int main_or_favourite = 0;

    public ClientConnection clientConnection;
    public boolean isLink;

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("CCavaj", "MainScreenActivity - onServiceDisconnected");
            isLink = false;
            clientConnection = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("CCavaj", "MainScreenActivity - onServiceConnected");
            isLink = true;
            ClientConnection.LocalBinder mLocalBinder = (ClientConnection.LocalBinder) service;
            clientConnection = mLocalBinder.getService();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pager = (ViewPager) findViewById(R.id.pager);
        loadListNames(infoItemArray);
        infoItemArray.shuffle();
        mainArray = infoItemArray;
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), infoItemArray);
        pager.setAdapter(pagerAdapter);


        ImageView imgSearch = findViewById(R.id.img_search);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogSearch();
            }
        });

        ImageView imgMenu = findViewById(R.id.img_menu);
        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        initNavigationView();

        titleName = findViewById(R.id.txt_title);
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
                    }

                    if ((lastPosition + MAX_PAGE_LIMIT) < PAGE_COUNT && infoItemArray.get(lastPosition + MAX_PAGE_LIMIT).getImageItem() == null) {
                        Log.d("myLogs", "> add position = " + (lastPosition + MAX_PAGE_LIMIT));
                        load(lastPosition + MAX_PAGE_LIMIT, infoItemArray);
                    }

                } else {

                    if ((position + MAX_PAGE_LIMIT + 1) < PAGE_COUNT && infoItemArray.get(position + MAX_PAGE_LIMIT + 1).getImageItem() != null) {
                        Log.d("myLogs", "< delete position = " + (position + MAX_PAGE_LIMIT + 1));
                        infoItemArray.get(position + MAX_PAGE_LIMIT + 1).recycle();
                    }

                    if (lastPosition >= MAX_PAGE_LIMIT && (infoItemArray.get(lastPosition - MAX_PAGE_LIMIT).getImageItem() == null)) {
                        Log.d("myLogs", "< add position = " + (lastPosition - MAX_PAGE_LIMIT));
                        load(lastPosition - MAX_PAGE_LIMIT, infoItemArray);
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

        public MyFragmentPagerAdapter(FragmentManager fm, InfoItemArray infoItemArray) {
            super(fm);

            for (int i = 0; i < MAX_PAGE_LIMIT + 1; i++) {

                if (i >= PAGE_COUNT)
                    break;

                load(i, infoItemArray);
            }

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

    void load(final int index, final InfoItemArray infoItemArray) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if ((infoItemArray.size() == 0) || (infoItemArray.get(index).getImageItem() != null))
                    return;

                try {
                    String wikipeadiaApiJson;
                    String responseSB;
                    HttpURLConnection httpcon;
                    BufferedReader in;

                    String encoding = "UTF-8";
                    try {

                        boolean b = true;
//                        String randomValue = listNames.get((int) (Math.random() * listNames.size()));
                        String randomValue = infoItemArray.get(index).getNameItem();
                        Log.d("myLogs", "load, position = " + index + ", name = " + randomValue);

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
                            if (m2.group(2) != null && m2.group(2).equals("extract")) {
                                text += "<big><big><b><font color=#FFFFFF>Основная информация</font></b></big></big><br>" + m2.group(3);
                                text += "<br><br><br>";
                            } else {
                                if (!(m2.group(5).replace("=", "").replace("\n", "").trim().equals("Галерея") ||
                                        m2.group(5).replace("=", "").replace("\n", "").trim().equals("См. также"))) {
                                    if (m2.group(6).charAt(0) == '\\') {
                                        text += "<big><big><b><font color=#FFFFFF>" + m2.group(5).replace("=", "").replace("\n", "").trim() + "</font></b></big></big><br>" + m2.group(6).substring(2);
                                    } else {
                                        text += "<big><big><b><font color=#FFFFFF>" + m2.group(5).replace("=", "").replace("\n", "").trim() + "</font></b></big></big><br>" + m2.group(6);
                                    }
                                    text += "<br><br><br>";
                                }
                            }

                            b = false;
                        }
                        if (b) {
                            Log.d("myLogs", "Информации не найдено - " + infoItemArray.get(index).getNameItem());
                            text = "Информации не найдено";
                        }
                        text = text.substring(0, text.length() - 8);
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
            int desiredWidth = 480; //Нужная ширина
            int desiredHeight = 800; //Нужная высота

            int scale;
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


    private void initNavigationView() {
        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigation);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.item_favourite:

                        onClickFavouriteItemMenu();

                        drawerLayout.closeDrawer(Gravity.START);
                        break;

                    case R.id.item_main:

                        main_or_favourite = 0;
                        infoItemArray = mainArray;
                        PAGE_COUNT = infoItemArray.size();
                        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), infoItemArray);
                        pager.setAdapter(pagerAdapter);
                        pager.setCurrentItem(mainLastPosition);

                        titleName.setText(Html.fromHtml("<big>Млекопитающие животные</big>"));
                        drawerLayout.closeDrawer(Gravity.START);

                        break;
                }

                return true;
            }
        });

    }

    void onClickFavouriteItemMenu(){

        mainLastPosition = lastPosition;
        main_or_favourite = 1;
        if (favouriteArray.size() == 0) {
            infoItemArray = favouriteArray;
            PAGE_COUNT = 1;
            pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), infoItemArray);
            pager.setAdapter(pagerAdapter);

        } else {
            infoItemArray = favouriteArray;
            PAGE_COUNT = infoItemArray.size();
            pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), infoItemArray);
            pager.setAdapter(pagerAdapter);
        }

        titleName.setText(Html.fromHtml("<big>Избранное</big>"));

    }

    void showDialogComment() {

        DialogComment dialogComment = new DialogComment();
        dialogComment.name = infoItemArray.get(lastPosition).getNameItem();
        dialogComment.show(getSupportFragmentManager(), "DialogComment");

    }

    void showDialogSearch() {

        DialogSearch dialogSearch = new DialogSearch();
        dialogSearch.show(getSupportFragmentManager(), "DialogSearch");

    }

    @Override
    protected void onPause() {
        if (isLink) {
            unbindService(serviceConnection);
            isLink = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ClientConnection.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}