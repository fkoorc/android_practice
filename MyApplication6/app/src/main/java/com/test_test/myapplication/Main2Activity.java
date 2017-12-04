package com.test_test.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.test_test.myapplication.R.id.button;
import static com.test_test.myapplication.R.id.button2;
import static com.test_test.myapplication.R.id.textView;

public class Main2Activity extends AppCompatActivity {
    private Button button2;
    private TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //取得此Button的實體
        button2 = (Button)findViewById(R.id.button2);

        textView2 = (TextView)findViewById(R.id.textView2);

        //實作OnClickListener介面

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化Intent物件
                Intent intent2 = new Intent();
                //從MainActivity 到Main2Activity
                intent2.setClass(Main2Activity.this , Main3Activity.class);
                //開啟Activity
                startActivity(intent2);

            }

        });

    }
}
