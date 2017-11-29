package com.wearable_test.myapplication;
//line 144  200 233 317 328 331 亂碼
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlA_distence extends Activity {
    private final static String TAG = DeviceControlA_distence.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    
    private ImageButton speak_btn,back_btn;
    
    private TextView mConnectionState;
    private static TextView mDistenceField,mColorField;
    private String mDeviceName;
    private String mDeviceAddress;
    static int intdis;
    private int back_count=0 ,speak_count=0 ;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    	ImageView imageView1;
    private TextToSpeech mSpeech = null;
    Menu_page mp;

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
                clearUI();
                
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	
               //顯示BLE SERVICE
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
				//如果打印為-2
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

        mDistenceField.setText(R.string.no_data);
        mSpeech.speak("已斷線",TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gatt_services_characteristics);
        setContentView(R.layout.d_control);
       // globalVariable = (GlobalVariable)getApplicationContext();   
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        final RelativeLayout background = (RelativeLayout)findViewById(R.id.back);
        mDistenceField = (TextView) findViewById(R.id.distence);
        mColorField = (TextView) findViewById(R.id.color_block);
        speak_btn = (ImageButton) findViewById(R.id.speaker);
        back_btn = (ImageButton) findViewById(R.id.back_btn);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        background.setBackgroundColor(Color.rgb(239, 228, 176));
        mColorField.setBackgroundColor(Color.rgb(239, 228, 176));
        mSpeech = new TextToSpeech(DeviceControlA_distence.this, new TTSListener());
        new Thread()
 		{
 			@Override
 			public void run()
 			{
 				super.run();  						     				
 				try {

 					Thread.sleep(100);
 					String edtSend = "aa1";
 					mBluetoothLeService.WriteValue(edtSend);
 					String edtSend2 = "ac1";
 					mBluetoothLeService.WriteValue(edtSend2);
 					
       	    	} 
 				catch (InterruptedException e1) 
 				{// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}  	  	          	
 		}.start();  
 		

        speak_btn.setOnClickListener(new Button.OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	
        	{  
        		back_count =0;
        		intdis=Integer.valueOf(BluetoothLeService.DT)/10;
        		mSpeech.speak("前方"+Integer.valueOf(BluetoothLeService.DT)/10+"公分有障礙物",TextToSpeech.QUEUE_FLUSH, null);
        		
        		}  	
       	});  
    
    back_btn.setOnClickListener(new Button.OnClickListener()
    {
    	@Override
    	public void onClick(View v)
    	
    	{  
    		if(back_count>=1){
    			back_count =0;
 	    	mConnected = false;
 	    	Thread.interrupted(); 
 	    	String edtSend = "aa0";
 			mBluetoothLeService.WriteValue(edtSend);
 			String edtSend2 = "ac0";
 			mBluetoothLeService.WriteValue(edtSend2);
 	    //	String uri = "@drawable/disconnected";
 	    //	int imageResource = getResources().getIdentifier(uri, null, getPackageName());
 	    //	Drawable image = getResources().getDrawable(imageResource);
 	    //	imageView1.setImageDrawable(image);

          //   updateConnectionState(R.string.disconnected);
             invalidateOptionsMenu();
             final Intent intent = new Intent();
             intent.setClass(DeviceControlA_distence.this, Menu_page.class);     
             startActivity(intent);
    		}
    		else
    		{
    			back_count=+1;
    			mSpeech.speak("返回主選單", TextToSpeech.QUEUE_FLUSH, null);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
    	    	mConnected = false;
    	    	Thread.interrupted();            	
                mBluetoothLeService.disconnect();                 
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }



 public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {     	
            switch (msg.what) {
            case 1:
                runOnUiThread(new Runnable() {
                    public void run() {     
                    	try{
                    	   mDistenceField.setText("距離:"+Integer.valueOf(BluetoothLeService.DT)+"mm");
                    	   }
                    	catch(Exception e){
                    		mDistenceField.setText("");
                    	}
                    	
                    	int adstart = BluetoothLeService.PW.indexOf("ad");   
                    	StringBuilder stringB1= new StringBuilder();
                    	StringBuilder stringB2= new StringBuilder();
                    	if (adstart!=-1){
                        	if(stringB1.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad10000"))
     	   mSpeech.speak("前方"+Integer.valueOf(BluetoothLeService.DT)/10+"公分有障礙物", TextToSpeech.QUEUE_FLUSH, null);
                        	
                        	else if(stringB2.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00010")){                       		
                        		mSpeech.speak("返回主選單", TextToSpeech.QUEUE_FLUSH, null);
                        		final Intent intent = new Intent();
                                intent.setClass(DeviceControlA_distence.this, Menu_page.class);              
                        		String edtSend = "aa0";
                     			mBluetoothLeService.WriteValue(edtSend);
                     			String edtSend2 = "ac0";
                     			mBluetoothLeService.WriteValue(edtSend2);
                     			try {
									Thread.sleep(1190);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                     			startActivity(intent);
                        	}
                    }            }       
                });
                break;     
            default:
                super.handleMessage(msg);
            }      
        }
    };
    
   
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
      //  intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
	 {
	    if ( keyCode ==  KeyEvent.KEYCODE_BACK)
	    {  
	    	return true;
	    }
	    return super.onKeyDown(keyCode, event);
	 }
}
