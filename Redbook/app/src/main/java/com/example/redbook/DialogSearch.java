package com.example.redbook;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DialogSearch extends DialogFragment {

    private EditText mInput;
    private Button mActionOk, mActionCancel;
    private ListView listView;
    List<ListSearchItem> list = new ArrayList<>();
    ArrayList<InfoItemArray.InfoItem> names;
    Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.dialog_search, container,false);
        mActionCancel = view.findViewById(R.id.btn_cancel);
        mActionOk = view.findViewById(R.id.btn_search);
        mInput = view.findViewById(R.id.edit_search);
        listView = view.findViewById(R.id.name_item_list);

        mInput.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_2.ttf"));

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        names = ((ViewPagerSampleActivity)getActivity()).infoItemArray.getInfoItems();

        list = new ArrayList<>();
        for (int i = 0; i < names.size(); i++){
                list.add(new ListSearchItem(names.get(i).getNameItem()));
        }
        listView.setAdapter(new ListSearchAdapter(getContext(),list));

        mInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                list = new ArrayList<>();

                for (int i = 0; i < names.size(); i++){
                    if (names.get(i).getNameItem().toLowerCase().contains(s.toString().toLowerCase())) {
                        list.add(new ListSearchItem(names.get(i).getNameItem()));
                        listView.setAdapter(new ListSearchAdapter(getContext(),list));
                    }
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mInput.setText(list.get(position).getNameItem());

                mActionOk.performClick();

            }
        });

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = mInput.getText().toString();
                if (!input.equals("")){

                    int index = ((ViewPagerSampleActivity)getActivity()).infoItemArray.getPositionFromName(input);
                    int maxPageLoad = ((ViewPagerSampleActivity)getActivity()).MAX_PAGE_LIMIT;
                    int lastPosition = ((ViewPagerSampleActivity)getActivity()).lastPosition;

                    if (index == -1){
                        Toast.makeText(getActivity(), "Не найдено", Toast.LENGTH_SHORT).show();
                        mInput.setText("");
                    }
                    else {

                        if (lastPosition == index){
                            getDialog().dismiss();
                            return;
                        }

                        if ((lastPosition - 1 == index) || (lastPosition + 1 == index)){
                            ((ViewPagerSampleActivity)getActivity()).pager.setCurrentItem(index);
                            getDialog().dismiss();
                            return;
                        }

                        if ((lastPosition - 2 == index) || (lastPosition + 2 == index)){
                            if (lastPosition > index)
                                ((ViewPagerSampleActivity)getActivity()).pager.setCurrentItem(index - 1);
                            else
                                ((ViewPagerSampleActivity)getActivity()).pager.setCurrentItem(index + 1);
                            ((ViewPagerSampleActivity)getActivity()).pager.setCurrentItem(index);
                            getDialog().dismiss();
                            return;
                        }


                        ((ViewPagerSampleActivity)getActivity()).infoItemArray.get(lastPosition).recycle();

                        for (int i = 1; i <= maxPageLoad; i++){
                            if ((lastPosition + i) < ((ViewPagerSampleActivity) getActivity()).infoItemArray.size()) {
                                ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(lastPosition + i).recycle();
                            }

                            if ((lastPosition - i) >= 0) {
                                ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(lastPosition - i).recycle();
                            }
                        }

                        ((ViewPagerSampleActivity)getActivity()).isLoad = false;
                        ((ViewPagerSampleActivity)getActivity()).load(index,((ViewPagerSampleActivity) getActivity()).infoItemArray);

                        for (int i = 1; i <= maxPageLoad; i++){
                            if ((index + i) < ViewPagerSampleActivity.PAGE_COUNT)
                                ((ViewPagerSampleActivity)getActivity()).load(index + i,((ViewPagerSampleActivity) getActivity()).infoItemArray);
                            if ((index - i) >= 0)
                                ((ViewPagerSampleActivity)getActivity()).load(index - i,((ViewPagerSampleActivity) getActivity()).infoItemArray);
                            if (i == 2) {
                                ((ViewPagerSampleActivity) getActivity()).pager.setCurrentItem(index);
                                getDialog().dismiss();
                            }
                        }




                    }

                }else {
                    Toast.makeText(getActivity(), "Вы ничего не ввели", Toast.LENGTH_SHORT).show();
                }

            }
        });

//        isSearch = true;
//        search();

        return view;
    }

//    private void search() {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String prevEdit = "";
//
//
//
//                while (isSearch){
//
//                    if (!mInput.getText().toString().equals(prevEdit)){
//
//                        String lastEdit = mInput.getText().toString();
//                        list = new ArrayList<>();
//
//                        for (int i = 0; i < names.size(); i++){
//                            if (names.get(i).getNameItem().contains(lastEdit)) {
//                                list.add(new ListSearchItem(names.get(i).getNameItem()));
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        listView.setAdapter(new ListSearchAdapter(getContext(),list));
//                                    }
//                                });
//                            }
//                            if (!lastEdit.equals(mInput.getText().toString()))
//                                break;
//                        }
//
//                    }
//
//                    prevEdit = mInput.getText().toString();
//                }
//            }
//        }).start();
//
//
//    }


}
