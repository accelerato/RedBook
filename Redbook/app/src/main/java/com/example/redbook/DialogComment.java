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
import android.widget.TextView;
import android.widget.Toast;

public class DialogComment extends DialogFragment {

    private TextView commentText;
    private EditText mInput;
    private Button mActionOk, mActionCancel;



    String name = "";
    Boolean isWait;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (((ViewPagerSampleActivity)getActivity()).clientConnection == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDialog().dismiss();
                }
            }).start();
            return null;
        }

        View view = inflater.inflate(R.layout.dialog_comment, container,false);
        mActionCancel = view.findViewById(R.id.quit);
        mActionOk = view.findViewById(R.id.send);
        mInput = view.findViewById(R.id.edit_comment);
        commentText = view.findViewById(R.id.txt_comment);

        commentText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_2.ttf"));
        mInput.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_2.ttf"));

        commentText.setMovementMethod(new ScrollingMovementMethod());

        commentText.setText("");
        load();

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
                final String input = mInput.getText().toString().replaceAll(" ","");
                if (!input.equals("")){

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            ((ViewPagerSampleActivity)getActivity()).clientConnection.sendString("send",name + ":" + "nick" + ":" + input);

                        }
                    }).start();

                    mInput.setText("");

                }else {
                    Toast.makeText(getActivity(), "Вы ничего не ввели", Toast.LENGTH_SHORT).show();
                }

            }
        });

        waitMessage();

        return view;
    }

    private void load() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ((ViewPagerSampleActivity)getActivity()).clientConnection.sendString("load",name);
            }
        }).start();

        String lastMessage;
        while (true){
            lastMessage = null;
            while (lastMessage == null)
                lastMessage = ((ViewPagerSampleActivity)getActivity()).clientConnection.keyValueString.get("load");

            if (lastMessage.equals(""))
                break;

            commentText.append(lastMessage + "\n");
        }

        if (commentText.getText().toString().equals(""))
            commentText.setText("Список комментариев пуст");

    }

    @Override
    public void onDestroyView() {
        isWait = false;
        super.onDestroyView();
    }

    void waitMessage(){
        isWait = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String lastMessage;
                while (isWait){

                    lastMessage = null;

                    try {
                        while (lastMessage == null){
                            lastMessage = ((ViewPagerSampleActivity)getActivity()).clientConnection.keyValueString.get("send");
                        }
                    }catch (NullPointerException e){
                        return;
                    }


                    if (lastMessage.equals(""))
                        break;

                    final String finalLastMessage = lastMessage;
                    ((ViewPagerSampleActivity)getActivity()).handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (commentText.getText().toString().equals("Список комментариев пуст"))
                                commentText.setText("");
                            commentText.append(finalLastMessage + "\n");
                        }
                    });

                }

            }
        }).start();

    }


}
