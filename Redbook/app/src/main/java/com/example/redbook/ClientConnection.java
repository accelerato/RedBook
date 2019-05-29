package com.example.redbook;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

public class ClientConnection extends Service {

    public final String DISCONNECT = "disconnect";

    public String LOGIN;
    private final String IP_ADDR = "212.16.19.18";
    private final int PORT = 8021;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;

    public KeyValueString keyValueString = new KeyValueString();
    boolean isRead = true;

    public class LocalBinder extends Binder {
        public ClientConnection getService() {
            return ClientConnection.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {

        onDisconnect();

        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, ClientConnection.class));
        createClient();
    }


    void createClient(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        socket = new Socket(IP_ADDR, PORT);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
                        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
                        readString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }).start();
    }

    public void onDisconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendString(DISCONNECT, " ");
                try {
                    socket.close();
                    stopSelf();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void readString() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRead) {

                    String keyValue;
                    try {
                        keyValue = in.readLine();

                        String key = "";
                        String value = "";
                        for (int i = 0; i != keyValue.length(); i++) {
                            if (keyValue.charAt(i) == '-') {
                                key = keyValue.substring(0, i);
                                value = keyValue.substring(i + 1);
                                break;
                            }
                        }
                        keyValueString.add(key,value);

                    } catch (IOException e) {
                        if (!isRead) return;
                    }
                }
            }
        }).start();
    }

    public synchronized boolean sendString(final String KEY, final String value) {
        try {
            out.write(KEY + "-" + value + "\r\n");
            out.flush();
            return true;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

}
