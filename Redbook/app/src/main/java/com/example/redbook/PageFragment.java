package com.example.redbook;


import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class PageFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";


    static PageFragment newInstance(int page) {
        Log.d("myLogs", "newInstance, page = " + page);
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ((ViewPagerSampleActivity)getActivity()).pager.setCurrentItem(getArguments().getInt(ARGUMENT_PAGE_NUMBER)+1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("myLogs", "onCreateView<<");
        final int index = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        View view = inflater.inflate(R.layout.fragment_main, null);
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView tvPage = view.findViewById(R.id.tvPage);

        if (((ViewPagerSampleActivity)getActivity()).infoItemArray.size() != 0) {

            while (((ViewPagerSampleActivity) getActivity()).infoItemArray.get(index).getImageItem() == null) {
//            Log.d("myLogs", "while - null");
                SystemClock.sleep(1);
            }




            tvPage.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_2.ttf"));

            TextView panel = view.findViewById(R.id.panel_txt);
            panel.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_2.ttf"));

//        tvPage.setMovementMethod(new ScrollingMovementMethod());
            panel.setText(Html.fromHtml("<big>" + ((ViewPagerSampleActivity) getActivity()).infoItemArray.get(index).getNameItem() + "</big>"));

            imageView.setImageBitmap(null);
            imageView.setImageBitmap(((ViewPagerSampleActivity) getActivity()).infoItemArray.get(index).getImageItem());

            tvPage.setText(Html.fromHtml(((ViewPagerSampleActivity) getActivity()).infoItemArray.get(index).getInfoItem()));

//        Log.d("myLogs", "onCreateView>>");

            ImageView imgComment = view.findViewById(R.id.img_comment);

            imgComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewPagerSampleActivity) getActivity()).showDialogComment();
                }
            });

            ImageView imgFavourite = view.findViewById(R.id.img_favourite);
            imgFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewPagerSampleActivity) getActivity()).favouriteArray.add(((ViewPagerSampleActivity) getActivity()).infoItemArray.get(index).getNameItem());
                }
            });

        }else {
            imageView.setImageBitmap(null);
            imageView.setImageResource(R.drawable.ic_favourite_empty);

            ImageView imgComment = view.findViewById(R.id.img_comment);
            imgComment.setImageResource(0);
            ImageView imgFavourite = view.findViewById(R.id.img_favourite);
            imgFavourite.setImageResource(0);

            SlidingUpPanelLayout sliding = view.findViewById(R.id.sliding_layout);
            sliding.setPanelHeight(0);
        }



        return view;
    }


}