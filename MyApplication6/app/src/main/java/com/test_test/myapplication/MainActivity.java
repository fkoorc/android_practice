package com.test_test.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener{

     Button button;
     TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //取得此Button的實體
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);

        button.setOnClickListener(this);
    }
        //實作OnClickListener介面



        //button.setOnClickListener(new View.OnClickListener() {});//動態建立物件登錄 只要一個時方便


            @Override
           public void onClick(View v) {

                if(v.getId() == R.id.button) {
                    //初始化Intent物件
                    Intent it_secondpage = new Intent();
                    //從MainActivity 到Main2Activity
                    it_secondpage.setClass(MainActivity.this, Main2Activity.class);
                    //開啟Activity
                    startActivity(it_secondpage);
                    textView.setText("pressed!");
                }
           }

        }


}
