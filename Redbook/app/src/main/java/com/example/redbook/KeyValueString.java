package com.example.redbook;

import java.util.ArrayList;

public class KeyValueString {
    class KeyValue{
        String key;
        String value;

        KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    private ArrayList<KeyValue> keyValues = new ArrayList<>();

    public synchronized void add(String key, String value){
        if (!key.equals("empty"))
            keyValues.add(new KeyValue(key,value));
    }

    public synchronized String get(String key){
        String value = null;
        for (KeyValue o : keyValues){
            if (o.key.equals(key)){
                value = o.value;
                keyValues.remove(o);
                break;
            }
        }
        return value;
    }
}
