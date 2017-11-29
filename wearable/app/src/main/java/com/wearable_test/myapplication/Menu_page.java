package com.wearable_test.myapplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Menu_page extends Activity {
    private final static String TAG = DeviceControlA_distence.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private ImageButton btn_distence, btn_color, btn_func, btn_call;
    public ProgressDialog myDialog = null;
    private TextView mConnectionState;
    private String mDeviceName, bestmac;
    private String mDeviceAddress;
    private int mDeviceRssi;
    String fileName = "Wearable_connect_mac.txt";
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    ImageView imageView1;
    //TTS
    int distance_count = 0, color_count = 0, long_count_call = 0, count_call, func_count = 0;
    int shake_count = 0;
    private TextToSpeech mSpeech = null;
    long test = 0;
    String phone_number, call_number;
    final Context context = this;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            BluetoothLeService.setActivityHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            final Intent intent = new Intent(Menu_page.this, DeviceScanActivity.class);
            mSpeech.speak("連線未完成，請重新連線", TextToSpeech.QUEUE_FLUSH, null);
            new AlertDialog.Builder(Menu_page.this)
                    .setCancelable(false)
                    .setPositiveButton("重新連線",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    startActivity(intent);
                                }
                            })
                    .show();
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                final Intent intent2 = new Intent(Menu_page.this, DeviceScanActivity.class);
                mSpeech.speak("連線未完成，請重新連線", TextToSpeech.QUEUE_FLUSH, null);
                new AlertDialog.Builder(Menu_page.this)
                        .setTitle(R.string.connect_error_title)
                        .setCancelable(false)
                        .setPositiveButton("重新連線",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        startActivity(intent2);
                                    }
                                })
                        .show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gatt_services_characteristics);
        setContentView(R.layout.menu_page);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        btn_distence = (ImageButton) findViewById(R.id.btn_dis);
        btn_color = (ImageButton) findViewById(R.id.btn_col);
        btn_call = (ImageButton) findViewById(R.id.btn_bat);
        btn_func = (ImageButton) findViewById(R.id.btn_func);
        //btn_search = (ImageButton) findViewById(R.id.btn_sear);
        // btn_reset_div = (ImageButton) findViewById(R.id.btn_reset_dev);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mSpeech = new TextToSpeech(Menu_page.this, new TTSListener());
        try {
            FileInputStream fileIn = openFileInput(fileName);
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[100];
            bestmac = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                bestmac += readstring;
            }
            InputRead.close();
            bestmac = bestmac.toString();
            if (bestmac.equals("")) {
                final Intent intent2 = new Intent(Menu_page.this, DeviceScanActivity.class);
                new AlertDialog.Builder(Menu_page.this)
                        .setTitle(R.string.connect_error_title)
                        .setMessage(R.string.connect_error_body)
                        .setCancelable(false)
                        .setPositiveButton("設定",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        startActivity(intent2);
                                    }
                                })
                        .show();

            }
        } catch (IOException e) {
            e.printStackTrace();
            bestmac = "";
        }
     /*   new Thread()
 		{
 			@Override
 			public void run()
 			{
 				super.run();  						     				
 				try {
 					Thread.sleep(400);
 					mBluetoothLeService.setPWNotification(true); 	
 					Thread.sleep(180);
 					mBluetoothLeService.WriteValue("aw1");
       	    	} 
 				catch (InterruptedException e1) 
 				{// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}  	  	          	
 		}.start();  */
        BluetoothLeService.setActivityHandler(mHandler);
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
        btn_color.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (color_count >= 1) {
                    color_count = 0;
                    final Intent intent = new Intent();
                    intent.setClass(Menu_page.this, DeviceControlB_color.class);

                    startActivity(intent);
                } else {
                    color_count = +1;
                    mSpeech.speak("顏色偵測", TextToSpeech.QUEUE_FLUSH, null);
                    distance_count = 0;
                    func_count = 0;
                    count_call = 0;
                    long_count_call = 0;
                }
            }
        });
        btn_func.setOnClickListener(new Button.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (func_count >= 1) {
                                                func_count = 0;
                                                Intent intent = new Intent();
                                                intent.setClass(Menu_page.this, Setting.class);
                                                startActivity(intent);
                                            } else {
                                                func_count = +1;
                                                mSpeech.speak("其他功能", TextToSpeech.QUEUE_FLUSH, null);
                                                distance_count = 0;
                                                color_count = 0;
                                                count_call = 0;
                                                long_count_call = 0;
                                            }
                                        }
                                    }
        );
        //  長按設定、短按撥打
        btn_call.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                if (long_count_call >= 1) {
                    long_count_call = 0;
                    mSpeech.speak("請輸入欲設定的電話號碼", TextToSpeech.QUEUE_FLUSH, null);
	            	 /* Alert Dialog Code Start*/
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("設定撥打號碼");
                    alert.setMessage("輸入號碼");
                    final EditText input = new EditText(context);
                    alert.setView(input);
                    phone_number = input.getText().toString();
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            FileOutputStream fout;
                            try {
                                fout = openFileOutput("Wearable_call.txt", Context.MODE_PRIVATE);
                                OutputStreamWriter outputWriter = new OutputStreamWriter(fout);
                                try {
                                    outputWriter.write(input.getText().toString());
                                    Log.w("write", input.getText().toString());
                                    outputWriter.close();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    });
                    alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mSpeech.speak("取消設定", TextToSpeech.QUEUE_FLUSH, null);
                            dialog.cancel();
                        }
                    }); //End of alert.setNegativeButton
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
	           /* Alert Dialog Code End*/
                } else {
                    long_count_call = +1;
                    distance_count = 0;
                    color_count = 0;
                    count_call = 0;
                    func_count = 0;
                    mSpeech.speak("設定撥打電話", TextToSpeech.QUEUE_FLUSH, null);
                }
                return true;
            }
        });
        btn_call.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {
                    String fileName = "Wearable_call.txt";
                    FileInputStream fileIn = openFileInput(fileName);
                    InputStreamReader InputRead = new InputStreamReader(fileIn);

                    char[] inputBuffer = new char[100];
                    call_number = "";
                    int charRead;

                    while ((charRead = InputRead.read(inputBuffer)) > 0) {
                        // char to string conversion
                        String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                        call_number += readstring;
                    }
                    InputRead.close();
                    call_number = call_number.toString();
                } catch (IOException e) {
                    // e.printStackTrace();
                    Log.e("call", " error ");
                }
                if (count_call >= 1) {
                    if (call_number.equals("")) {
                        mSpeech.speak("請先長按兩次設定撥打電話", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        count_call = 0;
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + call_number));
                        if (ActivityCompat.checkSelfPermission(Menu_page.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        startActivity(callIntent);

	                    }
               	   } 
            	else
            	{
            		count_call =+1;
            		distance_count =0 ;color_count =0 ; func_count=0 ;long_count_call=0;
            		mSpeech.speak("撥打電話", TextToSpeech.QUEUE_FLUSH , null);
            		Log.w("number",call_number+"");
            	}
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        new Thread()
 		{
 			@Override
 			public void run()
 			{
 				super.run();  						     				
 				try {
 					Thread.sleep(1000);
 					mBluetoothLeService.setPWNotification(true); 	
 					Thread.sleep(400);
 					mBluetoothLeService.WriteValue("aw1");
       	    	} 
 				catch (InterruptedException e1) 
 				{// TODO Auto-generated catch block
 					Intent intent = new Intent();
        			intent.setClass(Menu_page.this,DeviceScanActivity.class);
        			startActivity(intent);
 				}
 				catch (Error e2) 
 				{// TODO Auto-generated catch block
 					Intent intent = new Intent();
        			intent.setClass(Menu_page.this,DeviceScanActivity.class);
        			startActivity(intent);
 				}
 			}  	  	          	
 		}.start();  
 		BluetoothLeService.setActivityHandler(mHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
    	if (mSpeech != null) {
			mSpeech.stop();
			mSpeech.shutdown();
			mSpeech = null;}
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

	private class TTSListener implements OnInitListener {

		@Override
		public void onInit(int status) {
			// TODO Auto-generated method stub
			if (status == TextToSpeech.SUCCESS) {
				//int result = mSpeech.setLanguage(Locale.ENGLISH);
				int result = mSpeech.setLanguage(Locale.CHINESE);
				//彆湖荂峈-2ㄛ佽隴祥盓厥涴笱逄晟
//				
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					Log.w("tts","nothing ");
					Toast.makeText(Menu_page.this, "Can't Speak Chineses " + result, Toast.LENGTH_LONG).show();
				} else {
					Log.w("tts","got it ");
					//mSpeech.speak("i love you", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		}

	}


 @SuppressLint("HandlerLeak")
public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	
            switch (msg.what) {
            case 1:
                runOnUiThread(new Runnable() {
                    public void run() {
                    	int adstart = BluetoothLeService.PW.indexOf("ad");
                    	//Log.w("adstat", adstart+"");
                    	StringBuilder stringB1= new StringBuilder();
                    	StringBuilder stringB2= new StringBuilder();
                    	StringBuilder stringB3= new StringBuilder();
                    	StringBuilder stringB4= new StringBuilder();
                    	StringBuilder stringB5= new StringBuilder();
                    	mBluetoothLeService.readRemoteRssi();
                    	if (adstart!=-1){
                    	if(stringB1.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00001"))
                    	{    
                    		  Intent intent = new Intent();
                    		  stringB1.setLength(0);
                    		  mSpeech.speak("距離偵測", TextToSpeech.QUEUE_FLUSH, null);
                              intent.setClass(Menu_page.this, DeviceControlA_distence.class);
                		      startActivity(intent); 
                		}
                    	else if(stringB2.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00100"))
                    	{
                    		Intent intent = new Intent();
                    		stringB2.setLength(0);
                    		mSpeech.speak("顏色偵測", TextToSpeech.QUEUE_FLUSH, null);
                            intent.setClass(Menu_page.this, DeviceControlB_color.class);
                    		startActivity(intent);
                    	}
                    //	else if(stringB3.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00010"))
                    //	{
                    //		stringB3.setLength(0);
                    //		mSpeech.speak("手環關閉", TextToSpeech.QUEUE_FLUSH, null);
                    //	}
                    	else if(stringB4.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad01000"))
                    	{
                    		stringB4.setLength(0);
                    		mSpeech.speak("手機在這裡", TextToSpeech.QUEUE_FLUSH, null);
                    	//撥放音樂
                    	}
               //     	else if(stringB5.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad10000"))
               //     	{
               //     		stringB5.setLength(0);
                //    		mSpeech.speak("緊急"+"這裡需要幫助", TextToSpeech.QUEUE_FLUSH, null);
                //    	}
                    	  // mDistenceField.setText(BluetoothLeService.DT+"\n"
                    	  // +"R:"+BluetoothLeService.R+"\n"+"G:"+BluetoothLeService.G+"\n"+"B:"+BluetoothLeService.B+"\n"); 
                    	 //  mColorField.setBackgroundColor(Color.rgb(BluetoothLeService.R, BluetoothLeService.G, BluetoothLeService.B));
                    	  
                    	//utts.say(BluetoothLeService.PW);
                    	Log.e("tab",stringB1.append(BluetoothLeService.PW,adstart,adstart+7).toString());
                    	stringB1.setLength(0);
                    	}          
                 //   short rssi = getIntent().getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                  //  Log.w("string",BluetoothLeService.rssi+"");
                    /*	try{
                    	rssi_value.setText(String.valueOf(BluetoothLeService.distance));
                    	}
                 	catch(Exception e){
                 		rssi_value.setText("");
                 	}           */
                    }
                });
                break;
            
            case 2:    
            	runOnUiThread(new Runnable() {
                    public void run() {
                    	String edtSend;
                    	int dis = BluetoothLeService.distance;
                    	int adstart = BluetoothLeService.PW.indexOf("ad");
                    		//mBluetoothLeService.readRemoteRssi();
    					if(dis<33)
    						{
    						edtSend = "";
    						shake_count++;
    						if(shake_count==2)
    						{
    							edtSend = "ag001001";
    							shake_count=0;
    						}
    						}
    				/*	else if(dis>25 && dis<30)
                       	 {edtSend = "";
                       	 shack_counter=1;
                       	 if(shack_counter==1)
                       		{edtSend = "ag001001";
                       		shack_counter=0;} 
                       		}
    					else if(dis>30 && dis<35)
                       	 edtSend = "ag001001";*/
    					else
                       	 edtSend = "ag001002";						
                    	mBluetoothLeService.WriteValue(edtSend);
                    	
                    	if(adstart!=-1)
                  	         BluetoothLeService.flag=true;	
                    	/*try{
                        	rssi_value.setText(String.valueOf(BluetoothLeService.distance));
                        	}
                     	catch(Exception e){
                     		rssi_value.setText("");
                     	}           */
            }
                });
            
               break;
            default:
                super.handleMessage(msg);
            }
            
          
        }
    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;   
        }   
        return super.onKeyDown(keyCode, event);   
    }
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(Menu_page.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
            	moveTaskToBack(true);
  
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//示對話框
    }
    public void search_stop(){
    	Thread.interrupted();
    	BluetoothLeService.flag=true;
    	Log.w("finish","");
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
      //  intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
    
