package com.test.ble_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class third_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_page);
    }
    public void goback2(View v){
        Intent gob3 = new Intent(this,second_page.class);
        startActivity(gob3);
        third_page.this.finish();

    }


}
