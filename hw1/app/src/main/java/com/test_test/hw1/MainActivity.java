package com.test_test.hw1;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    //宣告代表UI變數
    EditText sname,gender,phone;
    TextView txv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //初始化變數
        sname = (EditText) findViewById(R.id.name);
        gender = (EditText) findViewById(R.id.gender);
        phone = (EditText) findViewById(R.id.phone);
        txv = (TextView) findViewById(R.id.txv);
    }

    public void onclick(View v){
        txv.setText(sname.getText().toString()+
                    "的性別是" + gender.getText()+
                    "的電話是" + phone.getText());


    }

    int size = 30;//字型大小初始值30sp
    public void bigger(View v){
        txv = (TextView) findViewById(R.id.txv);
        txv.setTextSize(++size);

    }
    public void smaller(View v){
        if(size>30) {
            txv = (TextView) findViewById(R.id.txv);
            txv.setTextSize(--size);
        }
    }

}

