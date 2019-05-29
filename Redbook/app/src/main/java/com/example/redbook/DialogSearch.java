package com.example.redbook;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DialogSearch extends DialogFragment {

    private EditText mInput;
    private Button mActionOk, mActionCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_search, container,false);
        mActionCancel = view.findViewById(R.id.btn_cancel);
        mActionOk = view.findViewById(R.id.btn_search);
        mInput = view.findViewById(R.id.edit_search);

        mInput.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_2.ttf"));

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


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

                        ((ViewPagerSampleActivity)getActivity()).infoItemArray.get(lastPosition).recycle();
                        ((ViewPagerSampleActivity)getActivity()).infoItemArray.get(lastPosition).setImageItem(null);

                        for (int i = 1; i <= maxPageLoad; i++){
                            if ((lastPosition + i) < ((ViewPagerSampleActivity) getActivity()).infoItemArray.size()) {
                                ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(lastPosition + i).recycle();
                                ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(lastPosition + i).setImageItem(null);
                            }

                            if ((lastPosition - i) >= 0) {
                                ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(lastPosition - i).recycle();
                                ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(lastPosition - i).setImageItem(null);
                            }
                        }

                        ((ViewPagerSampleActivity)getActivity()).isLoad = false;
                        ((ViewPagerSampleActivity)getActivity()).load(index);

                        for (int i = 1; i <= maxPageLoad; i++){
                            if ((index + i) < ViewPagerSampleActivity.PAGE_COUNT)
                                ((ViewPagerSampleActivity)getActivity()).load(index + i);
                            if ((index - i) >= 0)
                                ((ViewPagerSampleActivity)getActivity()).load(index - i);
                        }

                        ((ViewPagerSampleActivity)getActivity()).pager.setCurrentItem(index);
                        getDialog().dismiss();
                    }

                }else {
                    Toast.makeText(getActivity(), "Вы ничего не ввели", Toast.LENGTH_SHORT).show();
                }

            }
        });


        return view;
    }



}
