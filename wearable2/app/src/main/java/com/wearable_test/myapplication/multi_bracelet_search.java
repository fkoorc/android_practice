package com.wearable_test.myapplication;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;

/**
 * Created by sony on 2017/11/3.
 */

public class multi_bracelet_search extends Activity {


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private Button bracelet1, bracelet2, bracelet3, goback;//change to ImageButton

    private String mDeviceName;
    private String mDeviceAddress;
    private int count_b1 =0, count_b2=0,count_b3=0,count_goback=0;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private TextToSpeech mSpeech = null;
    final Context context = this;

    private class TTSListener implements OnInitListener {
        @Override
        public void onInit(int status) {
            // TODO Auto-generated method stub
            if (status == TextToSpeech.SUCCESS) {
                //int result = mSpeech.setLanguage(Locale.ENGLISH);
                int result = mSpeech.setLanguage(Locale.CHINESE);

                //Toast.makeText(DeviceControlA_distence.this, "-------------result = " + result, Toast.LENGTH_LONG).show();
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("tts","nothing ");
                } else {
                    Log.w("tts","got it ");
                    //mSpeech.speak("i love you", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }

    }
    private void clearUI() {
        mSpeech.speak("已斷線", TextToSpeech.QUEUE_FLUSH, null);
    }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gatt_services_characteristics);
        setContentView(R.layout.multi_bracelet_search);
        final Intent intent = getIntent();
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        //mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        bracelet1 = (Button)findViewById(R.id.brace1);
        bracelet2 = (Button)findViewById(R.id.brace2);
        bracelet3 = (Button)findViewById(R.id.brace3);
        goback = (Button)findViewById(R.id.goback);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mSpeech = new TextToSpeech(this, new multi_bracelet_search.TTSListener());



//回上一頁
        goback.setOnClickListener(new Button.OnClickListener() {
                                      @Override
                                      public void onClick(View v){
                                          if( count_goback >=1 )
                                          {
                                              count_goback = 0;
                                              Intent intent = new Intent();
                                              intent.setClass(multi_bracelet_search.this,Setting.class);
                                              startActivity(intent);
                                          }
                                          else
                                          {
                                              count_goback =+1;
                                              mSpeech.speak("回到設定", TextToSpeech.QUEUE_FLUSH , null);
                                              count_b1 =0; count_b2=0; count_b3=0;
                                          }


                                      }
                                  }
        );
//如果按兩下Button 藍芽找到的手環會震動
        bracelet1.setOnClickListener(new Button.OnClickListener() {
                                      @Override
                                      public void onClick(View v){
                                          if( count_b1 >=1 )
                                              {
                                                  count_b1 = 0;
                                                  BluetoothLeService.flag=true;

                                                  //如果有連上如果有連上BluetoothLeService就震動1秒
                                                  Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                                                  Log.d("TAG", "hasVibrator = " + vibrator.hasVibrator());
                                                  vibrator.vibrate(1000);//震1秒

                                                  mBluetoothLeService.WriteValue("ag");//ai1改成ag
                                                  if(!mConnected) return;
                                              }
                                              else
                                              {
                                                  count_b1 =+ 1;
                                                  mSpeech.speak("尋找手環", TextToSpeech.QUEUE_FLUSH, null);
                                                  count_b2=0; count_b3=0; count_goback=0;
                                              }


                                      }
                                  }
        );

        bracelet2.setOnClickListener(new Button.OnClickListener() {
                                      @Override
                                      public void onClick(View v){
                                          if( count_b2 >=1 )
                                          {
                                              count_b2 = 0;
                                              BluetoothLeService.flag=true;

                                              //如果有連上如果有連上BluetoothLeService就震動1秒
                                              Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                                              Log.d("TAG", "hasVibrator = " + vibrator.hasVibrator());
                                              vibrator.vibrate(1000);//震1秒

                                              mBluetoothLeService.WriteValue("ag");//ai1改成ag
                                              if(!mConnected) return;
                                          }
                                          else
                                          {
                                              count_b2 =+ 1;
                                              mSpeech.speak("尋找手環", TextToSpeech.QUEUE_FLUSH, null);
                                              count_b1=0; count_b3=0; count_goback=0;
                                          }


                                      }
                                  }
        );

        bracelet3.setOnClickListener(new Button.OnClickListener() {
                                      @Override
                                      public void onClick(View v){
                                          if( count_b3 >=1 )
                                          {
                                              count_b3 = 0;
                                              BluetoothLeService.flag=true;

                                              //如果有連上如果有連上BluetoothLeService就震動1秒
                                              Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                                              Log.d("TAG", "hasVibrator = " + vibrator.hasVibrator());
                                              vibrator.vibrate(1000);//震1秒

                                              mBluetoothLeService.WriteValue("ag");//ai1改成ag
                                              if(!mConnected) return;
                                          }
                                          else
                                          {
                                              count_b3 =+ 1;
                                              mSpeech.speak("尋找手環", TextToSpeech.QUEUE_FLUSH, null);
                                              count_b1=0; count_b2  =0; count_goback=0;
                                          }


                                      }
                                  }
        );
/*
        btn_distence.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (distance_count >= 1) {
                    distance_count = 0;
                    Intent intent = new Intent();
                    intent.setClass(Menu_page.this, DeviceControlA_distence.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", "distence");
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    distance_count = +1;
                    mSpeech.speak("距離偵測", TextToSpeech.QUEUE_FLUSH, null);
                    color_count = 0;
                    func_count = 0;
                    count_call = 0;
                    long_count_call = 0;
                }
            }
        });
    */
    }
}