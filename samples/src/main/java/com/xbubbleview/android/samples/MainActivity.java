package com.xbubbleview.android.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.xbubbleview.android.XBubbleView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private static final int[] IMAGES = {
            R.drawable.icon1,
            R.drawable.icon2,
            R.drawable.icon3,
            R.drawable.icon4,
            R.drawable.icon5
    };

    private XBubbleView bubbleView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bubbleView = (XBubbleView) findViewById(R.id.view_bubble);
        bubbleView.setImageResources(IMAGES);

        ((RadioGroup) findViewById(R.id.radio_group)).setOnCheckedChangeListener(this);

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                bubbleView.startAnimation();
                break;

            case R.id.btn2:
                bubbleView.stopAnimation();
                break;

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        RadioButton rbtn = (RadioButton) radioGroup.findViewById(checkedId);
        String text = (String) rbtn.getText();
        bubbleView.setCount(Integer.valueOf(text));
    }
}
