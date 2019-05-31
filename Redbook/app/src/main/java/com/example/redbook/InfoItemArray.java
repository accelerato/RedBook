package com.example.redbook;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;

class InfoItemArray {

    private ArrayList<InfoItem> infoItems = new ArrayList<>();

    void add(String nameItem){
        infoItems.add(new InfoItem(nameItem));
    }

    InfoItem get(int index){
        return infoItems.get(index);
    }

    void shuffle(){
        Collections.shuffle(infoItems);
    }

    int getPositionFromName(String nameItem){


        for (int i = 0; i < infoItems.size(); i++){
            if (infoItems.get(i).getNameItem().equals(nameItem)){
                return i;
            }
        }

        return -1;

    }

    ArrayList<InfoItem> getInfoItems(){
        return infoItems;
    }

    int size(){
        return infoItems.size();
    }

    public class InfoItem {

        private final String nameItem;
        private Bitmap imageItem;
        private String infoItem;

        InfoItem(String nameItem) {
            this.nameItem = nameItem;
            this.imageItem = null;
            this.infoItem = "";
        }

        String getNameItem() {
            return nameItem;
        }

        Bitmap getImageItem() {
            return imageItem;
        }

        void recycle(){
            if (imageItem != null && !imageItem.isRecycled()) {
                imageItem.recycle();
                imageItem = null;
            }
        }

        synchronized void setImageItem(Bitmap imageItem) {
            this.imageItem = imageItem;
        }

        String getInfoItem() {
            return infoItem;
        }

        void setInfoItem(String infoItem) {
            this.infoItem = infoItem;
        }

    }
}
