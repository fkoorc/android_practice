package com.test.ble_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class second_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_page);

    }
        //Intent it = new Intent();
        //it.setClass(this, Act2.class);
        //startActivity(it);
        public void goback1(View v){
            Intent gob1 = new Intent(this,MainActivity.class);
            startActivity(gob1);
            second_page.this.finish();
        }
    public void gothird(View v){
        Intent gob2 = new Intent(this,third_page.class);
        startActivity(gob2);
        second_page.this.finish();
    }
}
