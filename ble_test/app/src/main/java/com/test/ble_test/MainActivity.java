package com.test.ble_test;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
//import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{

   /* implements OnFlagMsgListener {
        FlagBt bt;
        TextView txv;
        byte[] ledCmd = {
                (byte) 0XFF,

        };
*/
        TextView txv;
        Button btn;
        Button btn2;
        Button btn3;
        Toast tos;
        int counter = 0;
        private class Myclicklis implements View.OnClickListener, View.OnLongClickListener{
            public void onClick(View v){
                if(v.getId() == R.id.button2) {
                    counter = counter + 2;

                    txv.setText(String.valueOf(counter));

                    //Toast.makeText(MainActivity.this,"show 3 seconds only"+counter,Toast.LENGTH_LONG);
                    //tos.show();
                }
                else if(v.getId() == R.id.button)
                {
                    txv.setText(String.valueOf(++counter));
                }
                else if(v.getId() == R.id.button3)
                {

                    Intent it = new Intent(MainActivity.this,second_page.class);
                    startActivity(it);
                    MainActivity.this.finish();
                }

            }
            public boolean onLongClick(View v){
                if(v.getId() == R.id.txv) {
                    counter = 0;
                    txv.setText("我想吃掉你的胰臟.");
                }
                else if(v.getId() == R.id.button2)
                {
                    txv.setText(String.valueOf("please just click one time asshole"));

                }

                return true; //傳回true表示事件處理完畢 如果是false責成是會跑一次onclick 表示txv 為1

            }

        }
        //建立監聽事件
        View.OnClickListener CL = new Myclicklis(); //CL = onclicklistener
        View.OnLongClickListener LCL = new Myclicklis(); //LCL = onLongclicklistener




        @Override
        protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txv = (TextView)findViewById(R.id.txv);
        btn = (Button)findViewById(R.id.button);
        btn2 = (Button)findViewById(R.id.button2);
        btn3 = (Button)findViewById(R.id.button3);


            //每個物件都要登錄監聽物件 如果你想要對應的功能的話
            //登錄監聽物件 thsi表示mainacticity本身
            btn.setOnClickListener(CL);
            btn2.setOnClickListener(CL);
            btn3.setOnClickListener(CL);
            txv.setOnLongClickListener(LCL);
            btn2.setOnLongClickListener(LCL);



        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }
    public void secondpage(View v){


    }


}

