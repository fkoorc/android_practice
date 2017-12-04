package com.wearable_test.myapplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class Setting extends Activity {
	
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	private ImageButton btn_set , btn_search , btn_bat , btn_func ;
	private Button gomulti;
	private String mDeviceName;
    private String mDeviceAddress;
    private int count_func =0, count_search=0,battery_count=0,count_set=0;
    private int shake_count=0;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private TextToSpeech mSpeech = null;
	final Context context = this;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("Setting", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            BluetoothLeService.setActivityHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
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
                clearUI();
                
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	
               //顯示?��?��??�BLE SERVICE	
               // displayGattServices(mBluetoothLeService.getSupportedGattServices());
            	
            } 
        }
    };
	private class TTSListener implements OnInitListener {

		@Override
		public void onInit(int status) {
			// TODO Auto-generated method stub
			if (status == TextToSpeech.SUCCESS) {
				//int result = mSpeech.setLanguage(Locale.ENGLISH);
				int result = mSpeech.setLanguage(Locale.CHINESE);
				//�����ӡΪ-2��˵����֧����������
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
        setContentView(R.layout.setting);
        
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        btn_set = (ImageButton) findViewById(R.id.btn_set);
        btn_bat = (ImageButton) findViewById(R.id.btn_call);
        btn_func = (ImageButton) findViewById(R.id.btn_func);
        btn_search = (ImageButton) findViewById(R.id.btn_search);
		gomulti = (Button) findViewById(R.id.gomulti);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mSpeech = new TextToSpeech(Setting.this, new TTSListener()); 
        MyPhoneListener phoneListener = new MyPhoneListener();
        TelephonyManager telephonyManager =
       (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                // receive notifications of telephony state changes
        telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);

		Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);//10/30

        
        btn_set.setOnClickListener(new Button.OnClickListener()
        { 
        	@Override
        	public void onClick(View v)
        	{   
        		if(count_set >= 1 ) 
        		{
        			Intent intent = new Intent();
        			intent.setClass(Setting.this, DeviceScanActivity.class);
        	        FileOutputStream fout;
        			try 
        			{
        				fout = openFileOutput("Wearable_connect_mac.txt", Context.MODE_PRIVATE);
        				OutputStreamWriter outputWriter=new OutputStreamWriter(fout);
        				try 
        				{
        					outputWriter.write("");
        					outputWriter.close();
        					mBluetoothLeService.close();
        					mBluetoothLeService = null;
        					startActivity(intent);
        				} 
        				catch (IOException e) 
        				{
        			// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        	   	 
        			} catch (FileNotFoundException e) 
        			{
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}	

    			}
        		else 
        		{
          			 count_set =+ 1;
          			mSpeech.speak("重設裝置", TextToSpeech.QUEUE_FLUSH, null);
          			count_func =0 ; count_search=0 ; battery_count =0  ;
          		}
    		 }  	
       	});  
        
        btn_func.setOnClickListener(new Button.OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		if( count_func >=1 )
        		{
        			count_func = 0;
        			Intent intent = new Intent();
        			intent.setClass(Setting.this,Menu_page.class);
        			startActivity(intent);
        		}
        		else
        		{
        			count_func =+1;
        			mSpeech.speak("其他功能", TextToSpeech.QUEUE_FLUSH , null);
					count_search=0 ; battery_count=0 ; count_set=0 ;
        		}
        	}
        }
        );
        btn_bat.setOnClickListener(new Button.OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{   
        		if(battery_count >= 1 ) 
        		{
           		    battery_count = 0;
           		    mBluetoothLeService.WriteValue("ae");
        		if(!mConnected) return;

				//todo Send data
				} 
        		else 
        		{
        		    battery_count =+ 1;
        			mSpeech.speak("查詢電量", TextToSpeech.QUEUE_FLUSH, null);
        			count_search =0 ; count_func=0 ; count_set = 0 ;
    		 	}
        		
        	}
        });
        btn_search.setOnClickListener(new Button.OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{   
				if(count_search >= 1 )
        		{
        		    count_search = 0;
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
                    count_search =+ 1;
    			    mSpeech.speak("尋找手環", TextToSpeech.QUEUE_FLUSH, null);
    			    battery_count =0 ; count_func=0 ; count_set = 0 ;
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
       //     Log.d(TAG, "Connect request result=" + result);
        }
        new Thread()
 		{
 			@Override
 			public void run()
 			{
 				super.run();  						     				
 				try {
 					Thread.sleep(200);
 					mBluetoothLeService.setPWNotification(true); 
 					Thread.sleep(400);
 					mBluetoothLeService.setPWNotification(true); 	
       	    	} 
 				catch (InterruptedException e1) 
 				{// TODO Auto-generated catch block
 					e1.printStackTrace();
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

 @SuppressLint("HandlerLeak")
public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	
            switch (msg.what) {
            case 1:
                runOnUiThread(new Runnable() {
                    public void run() {
                    	int adstart = BluetoothLeService.PW.indexOf("ad");
                    	Log.w("adstat", adstart+"");
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
                              intent.setClass(Setting.this, DeviceControlA_distence.class);
                		      startActivity(intent); 
                		}
                    	else if(stringB2.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00100"))
                    	{
                    		Intent intent = new Intent();
                    		stringB2.setLength(0);
                    		mSpeech.speak("顏色偵測", TextToSpeech.QUEUE_FLUSH, null);
                            intent.setClass(Setting.this, DeviceControlB_color.class);
                    		startActivity(intent);
                    	}
               //     	else if(stringB3.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00010"))
              //      	{
               //     		stringB3.setLength(0);
               //     		mSpeech.speak("手環關閉", TextToSpeech.QUEUE_FLUSH, null);
               //     		}
                    	else if(stringB4.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad01000"))
                    	{
                    		stringB4.setLength(0);
                    		mSpeech.speak("手機在這裡", TextToSpeech.QUEUE_FLUSH, null);
                    	//撥放音樂
                    	}
               //     	else if(stringB5.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad10000"))
               //     	{
               //     		stringB5.setLength(0);
              //      		mSpeech.speak("緊急"+"這裡需要幫助", TextToSpeech.QUEUE_FLUSH, null);
                 //   	}
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
                    	int adstart = BluetoothLeService.PW.indexOf("ad");//改ag?
                    		mBluetoothLeService.readRemoteRssi();
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
    public void ConfirmExit(){//跳出視窗退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(Setting.this);
        ad.setTitle("離開}");
        ad.setMessage("確定要離開");
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
    private class MyPhoneListener extends PhoneStateListener {
		 
		private boolean onCall = false;
 
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
 
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			
			case TelephonyManager.CALL_STATE_OFFHOOK:
				onCall = true;
				break;

			case TelephonyManager.CALL_STATE_IDLE:
				// in initialization of the class and at the end of phone call 
				
				// detect flag from CALL_STATE_OFFHOOK
				if (onCall == true) {
					onCall = false;
				}
				break;
			default:
				break;
			}
			
		}
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