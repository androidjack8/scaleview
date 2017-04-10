package com.yhongm.scaleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.yhongm.scale_view_core.ScaleView;

public class MainActivity extends AppCompatActivity {
    ScaleView scaleView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scaleView = (ScaleView) findViewById(R.id.scale_view);
        textView = (TextView) findViewById(R.id.textView);
        scaleView.setMoveScaleListener(value -> textView.setText("选择了:" + value));
    }
}
