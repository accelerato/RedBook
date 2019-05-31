package com.example.redbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class ListSearchAdapter extends BaseAdapter {

    private List<ListSearchItem> list;
    private LayoutInflater layoutInflater;

    public ListSearchAdapter(Context context, List<ListSearchItem> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = layoutInflater.inflate(R.layout.item_search, parent, false);
        }

        ListSearchItem listSearchItem = (ListSearchItem) getItem(position);

        TextView groupID = view.findViewById(R.id.name_item_txt);
        groupID.setText(listSearchItem.getNameItem());

        return view;
    }
}
