package com.wearable_test.myapplication;

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
public class DeviceControlB_color extends Activity {
    private final static String TAG = DeviceControlA_distence.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    
    private ImageButton speak_btn,back_btn;
    private TextView mConnectionState;
    private static TextView mDistenceField,mDistenceField2,mColorField;
    private String mDeviceName;
    private String mDeviceAddress;
    private String color;
    private int back_count=0;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    ImageView imageView1;
    private TextToSpeech mSpeech = null;
  	Menu_page mp;
  	private String[] colorXX = new String[]{"紅色", "澄色", "黃色", "黃綠色", "綠色"
			,"青綠色", "青色", "藍青色", "藍色", "藍紫色"
			,"紫色", "紫紅色"};
  	private String[] brightnessZ = new String[]{"", "亮", "暗"};
  	
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
            	
               //�@ʾ?????????BLE SERVICE	
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
        mSpeech.speak("已斷線", TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gatt_services_characteristics);
        setContentView(R.layout.d_control);
        

        
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.

        final RelativeLayout background = (RelativeLayout)findViewById(R.id.back);
        mDistenceField = (TextView) findViewById(R.id.distence);
        mDistenceField2 = (TextView) findViewById(R.id.distence2);
        mColorField = (TextView) findViewById(R.id.color_block);
        speak_btn = (ImageButton) findViewById(R.id.speaker);
        back_btn = (ImageButton) findViewById(R.id.back_btn);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        background.setBackgroundColor(Color.rgb(153, 217, 234));
        mSpeech = new TextToSpeech(DeviceControlB_color.this, new TTSListener());
        new Thread()
 		{
 			@Override
 			public void run()
 			{
 				super.run();  				
 				try {
 					Thread.sleep(100);
 					String edtSend = "ab1";
 					mBluetoothLeService.WriteValue(edtSend);
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
        		if(BluetoothLeService.R>150 && BluetoothLeService.G<80 && BluetoothLeService.B<80)
        			mSpeech.speak("這是紅色", TextToSpeech.QUEUE_FLUSH, null);
        		else if(BluetoothLeService.G>BluetoothLeService.R && (BluetoothLeService.G-30)>BluetoothLeService.B)
        			mSpeech.speak("這是綠色", TextToSpeech.QUEUE_FLUSH, null);
        		else if(BluetoothLeService.G>BluetoothLeService.R && BluetoothLeService.B>100)
        			mSpeech.speak("這是藍色", TextToSpeech.QUEUE_FLUSH, null);
        		else if(BluetoothLeService.B<140 && (BluetoothLeService.R-BluetoothLeService.G)<70)  // && (BluetoothLeService.R-20)>BluetoothLeService.G&& BluetoothLeService.B>5
        			mSpeech.speak("這是黃色", TextToSpeech.QUEUE_FLUSH, null);
        		//else if(Math.abs(BluetoothLeService.R-BluetoothLeService.B)<20 && (BluetoothLeService.R+20)>BluetoothLeService.G && BluetoothLeService.G>50)
        		//	mSpeech.speak("這是紫色", TextToSpeech.QUEUE_FLUSH, null);
        		else if(BluetoothLeService.R>200 && BluetoothLeService.G>80 && BluetoothLeService.G<130 && BluetoothLeService.B<70)
        			mSpeech.speak("這是橘色", TextToSpeech.QUEUE_FLUSH, null);
        		else if(BluetoothLeService.R>240 && BluetoothLeService.G>240 && BluetoothLeService.B>240)
        			mSpeech.speak("這是白色", TextToSpeech.QUEUE_FLUSH, null);
        		else if(BluetoothLeService.R<10 && BluetoothLeService.G<10 && BluetoothLeService.B<10)
        			mSpeech.speak("這是黑色", TextToSpeech.QUEUE_FLUSH, null);
        		//mSpeech.speak("偵測到"+color, TextToSpeech.QUEUE_FLUSH, null);
        		
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
 	    	String edtSend = "ab0";
 			mBluetoothLeService.WriteValue(edtSend);
 	    //	String uri = "@drawable/disconnected";
 	    //	int imageResource = getResources().getIdentifier(uri, null, getPackageName());
 	    //	Drawable image = getResources().getDrawable(imageResource);
 	    //	imageView1.setImageDrawable(image);

          //   updateConnectionState(R.string.disconnected);
             invalidateOptionsMenu();
             final Intent intent = new Intent();
             intent.setClass(DeviceControlB_color.this, Menu_page.class);     
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
                    	

             
                    	/*mDistenceField.setText(
                    			   //BluetoothLeService.DT+"\n"+
                      				"R:"+BluetoothLeService.R + "\n" +
                      				"G:"+BluetoothLeService.G + "\n" +
                      				"B:"+BluetoothLeService.B + "\n"); 
                    		
                    	   
                    	 */
                    	//button control   
                    	int adstart = BluetoothLeService.PW.indexOf("ad"); 
                    	int ajstart = BluetoothLeService.PW.indexOf("aj");
                    	int abstart = BluetoothLeService.PW.indexOf("ab");
                       	StringBuilder stringB1= new StringBuilder();
                       	StringBuilder stringB2= new StringBuilder();
                       	StringBuilder stringXX = new StringBuilder();
                       	StringBuilder stringY = new StringBuilder();
                       	StringBuilder stringZ = new StringBuilder();
                       	
                
                       	
                       	if (adstart!=-1){
                           	if(stringB1.append(BluetoothLeService.PW,adstart,adstart+7).toString().equals("ad00010")){
                        //   		Log.e("get ad","go");
                           		mSpeech.speak("返回主選單", TextToSpeech.QUEUE_FLUSH, null);
                        		final Intent intent = new Intent();
                                intent.setClass(DeviceControlB_color.this, Menu_page.class);                                
                        		
                           		String edtSend = "ab0";
                     			mBluetoothLeService.WriteValue(edtSend);
                     			try {
									Thread.sleep(1200);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                     			startActivity(intent);
                           	}
                           	
                       	}
                       	else if(abstart!=-1){
                       		StringBuilder stringR= new StringBuilder();
                			StringBuilder stringG= new StringBuilder();
                			StringBuilder stringB= new StringBuilder();
                			int R = Integer.valueOf(stringR.append(BluetoothLeService.PW, abstart+2, abstart+5).toString());
                			int G = Integer.valueOf(stringG.append(BluetoothLeService.PW, abstart+5, abstart+8).toString());
                			int B = Integer.valueOf(stringB.append(BluetoothLeService.PW, abstart+8, abstart+11).toString());
                		
                     		mDistenceField.setText(
                       			   //BluetoothLeService.DT+"\n"+
                         				"R:"+R + "\n" +
                         				"G:"+G + "\n" +
                         				"B:"+B + "\n"); 
                         		mColorField.setBackgroundColor(Color.rgb(BluetoothLeService.R, BluetoothLeService.G, BluetoothLeService.B));
                        
                       	}
                       	else if(ajstart!=-1)
                       	{
                       		String receive = stringB2.append(BluetoothLeService.PW,ajstart,ajstart+2).toString();
                       		// 顏色
                       		String xx = stringXX.append(BluetoothLeService.PW,ajstart+2,ajstart+4).toString();
                       		// 飽和
                       		String y = stringY.append(BluetoothLeService.PW,ajstart+4,ajstart+5).toString();
                       		// 亮暗
                       		String z = stringZ.append(BluetoothLeService.PW,ajstart+5,ajstart+6).toString();
                       		
                  
                       		mDistenceField2.setText(
                       					"  X :" + xx + "\n" +
                         				"  Y :" + y  + "\n" +
                         				"  Z :" + z  + "\n"); 
                       		//Log.e("receive",receive + " "+ xx + " "+ y + " " + z);
                       		// UI control   
                       		
                 
                       		if(y.equals("1")){
                       			if(z.equals("0")){
                       				mSpeech.speak("這是灰色",TextToSpeech.QUEUE_FLUSH,null);
                       			}else if(z.equals("1")){
                       				mSpeech.speak("這是白色",TextToSpeech.QUEUE_FLUSH,null);
                       			}else if(z.equals("2")){
                       				mSpeech.speak("這是黑色",TextToSpeech.QUEUE_FLUSH,null);
                       			}
                       			
                       		}else{
                       			Log.e("Y and Z", y + ", " + z);
                       			if(xx.equals("00")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[0],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[0],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("這是" + brightnessZ[2] + colorXX[0],TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("01")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[1],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[1],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("這是咖啡色",TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("02")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[2],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[2],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){ // 暗黃=>咖啡色
                       					mSpeech.speak("這是咖啡色",TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("03")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[3],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[3],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("這是" + brightnessZ[2] + colorXX[3],TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("04")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[4],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[4],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("這是" + brightnessZ[2] + colorXX[4],TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("05")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[5],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[5],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("這是" + brightnessZ[2] + colorXX[5],TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("06")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("這是" + brightnessZ[0] + colorXX[6],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("這是" + brightnessZ[1] + colorXX[6],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("這是" + brightnessZ[2] + colorXX[6],TextToSpeech.QUEUE_FLUSH,null);
                       				}
                       				
                       			}else if(xx.equals("07")){
                       				Log.e("XX", "xx = " + xx);
                       				if(z.equals("0")){
                       					mSpeech.speak("?o?O" + brightnessZ[0] + colorXX[7],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("1")){
                       					mSpeech.speak("?o?O" + brightnessZ[1] + colorXX[7],TextToSpeech.QUEUE_FLUSH,null);
                       				}else if(z.equals("2")){
                       					mSpeech.speak("?o?O" + brightnessZ[2] + colorXX[7],TextToSpeech.QUEUE_FLUSH,null);
                       				}

								}else if(xx.equals("08")){
									Log.e("XX", "xx = " + xx);
									if(z.equals("0")){
										mSpeech.speak("這是" + brightnessZ[0] + colorXX[8],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("1")){
										mSpeech.speak("這是" + brightnessZ[1] + colorXX[8],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("2")){
										mSpeech.speak("這是" + brightnessZ[2] + colorXX[8],TextToSpeech.QUEUE_FLUSH,null);
									}

								}else if(xx.equals("09")){
									Log.e("XX", "xx = " + xx);
									if(z.equals("0")){
										mSpeech.speak("這是" + brightnessZ[0] + colorXX[9],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("1")){
										mSpeech.speak("這是" + brightnessZ[1] + colorXX[9],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("2")){
										mSpeech.speak("這是" + brightnessZ[2] + colorXX[9],TextToSpeech.QUEUE_FLUSH,null);
									}

								}else if(xx.equals("10")){
									Log.e("XX", "xx = " + xx);
									if(z.equals("0")){
										mSpeech.speak("這是" + brightnessZ[0] + colorXX[10],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("1")){
										mSpeech.speak("這是" + brightnessZ[1] + colorXX[10],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("2")){
										mSpeech.speak("這是" + brightnessZ[2] + colorXX[10],TextToSpeech.QUEUE_FLUSH,null);
									}

								}else if(xx.equals("11")){
									Log.e("XX", "xx = " + xx);
									if(z.equals("0")){
										mSpeech.speak("這是" + brightnessZ[0] + colorXX[0],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("1")){
										mSpeech.speak("這是" + brightnessZ[1] + colorXX[11],TextToSpeech.QUEUE_FLUSH,null);
									}else if(z.equals("2")){
										mSpeech.speak("這是" + brightnessZ[2] + colorXX[11],TextToSpeech.QUEUE_FLUSH,null);
									}
                       				
                       			}
                       			
                       		}
                       		
                       		
                       		/*
                       		if(receive.equals("aj0")){
                       			mSpeech.speak("這是黑色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj1")){
                       			mSpeech.speak("這是紅色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj2")){
                       			mSpeech.speak("這是綠色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj3")){
                       			mSpeech.speak("這是黃色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj4")){
                       			mSpeech.speak("這是藍色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj5")){
                       			mSpeech.speak("這是洋紅色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj6")){
                       			mSpeech.speak("這是青色",TextToSpeech.QUEUE_FLUSH,null);
                       		}else if(receive.equals("aj7")){
                       			mSpeech.speak("這是白色",TextToSpeech.QUEUE_FLUSH,null);
                       		}
                       		*/
                       	}
                       	}                
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
