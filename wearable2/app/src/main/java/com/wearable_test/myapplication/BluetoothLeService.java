/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wearable_test.myapplication;
import android.R.string;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.UUID;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_NOTIFY =
            UUID.fromString(SampleGattAttributes.NOTIFY);
    public final static UUID UUID_WRITE =
    		UUID.fromString(SampleGattAttributes.WRITE);
    public final static UUID UUID_SERVICE =
            UUID.fromString(SampleGattAttributes.SERVICE);

    //public static int HR = 0;
    public static String PW = new String();
    public static String DT = new String();
    //public static String RDT = new String();
    public static int rssi = 0;
    public static int R = 0;
    public static int G = 0;
    public static int B = 0;
    //public static int Rssi = 0;
    private static Handler mActivityHandler = null;
    public static boolean flag = true;
    public static int distance = 0 ;
    //private int data_timer = 0;
    static int[] data = new int[3];
    static int i = 0;
    
    // use color, saturation and illuminance to represent color
    //public static int X = 0, Y = 0, Z = 0;
    
    
    public BluetoothGattCharacteristic mNotifyCharacteristic = null,mWriteCharacteristic = null;
    
    public void WriteValue(String strValue)
    {
    	mWriteCharacteristic.setValue(strValue.getBytes());
    	mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
    	
    }
    
    
    
    public void findService(List<BluetoothGattService> gattServices)
    {
    //	Log.i(TAG, "Count is:" + gattServices.size());
    	for (BluetoothGattService gattService : gattServices) 
    	{
    	//	Log.i(TAG, gattService.getUuid().toString());
		//	Log.i(TAG, UUID_SERVICE.toString());
    		if(gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString()))
    		{
    			List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
    		//	Log.i(TAG, "Count is:" + gattCharacteristics.size());
    			for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) 
    			{
    				if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString()) && mWriteCharacteristic!=null )
    				{
    				//	Log.i(TAG, gattCharacteristic.getUuid().toString());
    				//	Log.i(TAG, UUID_NOTIFY.toString());
    					mNotifyCharacteristic = gattCharacteristic;
    					setCharacteristicNotification(gattCharacteristic, true);
    					broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
    					return;
    				}
    				if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_WRITE.toString()))
    				{
    					mWriteCharacteristic = gattCharacteristic;
    					return;
    				}
    			}
    		}
    	}
    }
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                findService(gatt.getServices());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
        	
            if (status == BluetoothGatt.GATT_SUCCESS) {
                uiUpdate(characteristic);
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
        								int status)
        {
        	Log.e(TAG, "OnCharacteristicWrite");
        	if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "write successfully");
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	Log.i(TAG, "onCharacteristicChanged");
            uiUpdate( characteristic);
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                         BluetoothGattDescriptor bd,
                                         int status) {
        	Log.e(TAG, "onDescriptorRead");
        }
        
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
        								 BluetoothGattDescriptor bd,
                                         int status) {
        	Log.e(TAG, "onDescriptorWrite");
        }
        
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b)
        {
        	Log.e(TAG, "onReadRemoteRssi");
        	rssi = a;
            calculateAccuracy(rssi+100);
        }
        
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int a)
        {
        	Log.e(TAG, "onReliableWriteCompleted");
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    
    private void uiUpdate(final BluetoothGattCharacteristic characteristic) {
    	Message msg; 
        final byte[] data =  characteristic.getValue();
     	String datastring = new String(data);
      	Log.e("", datastring); 
      	PW = datastring;
      	int aastart = datastring.indexOf("aa");
      	int abstart = datastring.indexOf("ab");
    	int acstart = datastring.indexOf("ac");
    	//	Log.e("bbbbbbb", aastart+"ab"+abstart);
    	if (data != null && data.length > 3) {
    		if(abstart!=-1){
    			StringBuilder stringR= new StringBuilder();
    			StringBuilder stringG= new StringBuilder();
    			StringBuilder stringB= new StringBuilder();
    			R = Integer.valueOf(stringR.append(datastring, abstart+2, abstart+5).toString());
    			G = Integer.valueOf(stringG.append(datastring, abstart+5, abstart+8).toString());
    			B = Integer.valueOf(stringB.append(datastring, abstart+8, abstart+11).toString());
    			//Log.e("test", stringR.append(s, abstart+2, abstart+4).toString());
    		}
    		if(aastart!=-1){
    			final StringBuilder stringDT= new StringBuilder();
    			//	Log.w("dt", Integer.valueOf(stringDT.append(s, aastart+2, aastart+6).toString())+"");
    			DT = stringDT.append(datastring, aastart+2, aastart+6).toString();
    			//	DT=String.valueOf(dt);
    		}
    	
    		if(acstart!=1){
    			final StringBuilder stringRDT = new StringBuilder();
    			//RDT =stringRDT.append(datastring,acstart+2,acstart+7).toString();
    		}else{
    			Log.w(",","datastring");
    		}
    	}
    
    	//
    	if(flag){
    		Log.i(TAG, "flag1");
   			msg = Message.obtain(mActivityHandler , 1); 
   			msg.sendToTarget();
   		}else{
   			Log.i(TAG, "flag2");
   			msg = Message.obtain(mActivityHandler , 2);
    		msg.sendToTarget();
    	}
    		
    
   }
     
    public static void setActivityHandler(Handler mHandler) {
        Log.i("===", "Activity Handler set");
        mActivityHandler = mHandler;
    }
    
    protected static void calculateAccuracy(int rssi) {
    	data[i]=rssi;    	
    	Log.i("", data[0]+""+data[1]+""+data[2]);    		
    	if(i==0)
    	{	
    		if(data[0]<=(data[1]+3) && data[0]>=(data[1]-3) && data[0]<=(data[2]+3) && data[0]>=(data[2]-3))
    			distance = data[0];
    		i=1;
    	}
    	else if(i==1)
    	{
    		if( data[1]<=(data[2]+3) && data[1]>=(data[2]-3) && data[1]<=(data[0]+3) && data[1]>=(data[0]-3))
    			distance = data[1];
    		i=2;
    	}
    	else if(i==2)
    	{
    		if(data[2]<=(data[0]+3) && data[2]>=(data[0]-3) && data[2]<=(data[1]+3) && data[2]>=(data[1]-3))
    			distance = data[1];
    		i=0;
    	}
    }

    class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_SERVICE.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    public void readRemoteRssi() {
        mBluetoothGatt.readRemoteRssi();
    }

		public void setPWNotification(boolean enabled) {
			
			String BatteryService="6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
			String Power="6E400003-B5A3-F393-E0A9-E50E24DCCA9E";		
			String PowerWrite ="6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
		//	String BatteryService="0000ffe0-0000-1000-8000-00805f9b34fb";
		//	String Power="0000ffe1-0000-1000-8000-00805f9b34fb";		
			
			if (mBluetoothAdapter == null || mBluetoothGatt == null) {
				Log.w(TAG, "BluetoothAdapter not initialized");
				return;
			}		
          	 BluetoothGattService pws = mBluetoothGatt.getService(UUID.fromString(BatteryService));
             if (pws == null) {
                 Log.e(TAG, "PW service not found!");
                 return;
             }
             BluetoothGattCharacteristic pwc = pws.getCharacteristic(UUID.fromString(Power));
             if (pwc == null) {
                 Log.e(TAG, "Power Level charateristic not found!");
             }
             BluetoothGattCharacteristic pwW = pws.getCharacteristic(UUID.fromString(PowerWrite));
             if (pwW == null) {
                 Log.e(TAG, "Power Level charateristic not found!");
             }
			mBluetoothGatt.setCharacteristicNotification(pwc, enabled);
			mBluetoothGatt.setCharacteristicNotification(pwW, enabled);
			BluetoothGattDescriptor descriptor1 = pwc.getDescriptor(
			UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));			
			descriptor1.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor1);
			
			BluetoothGattDescriptor descriptor2 = pwc.getDescriptor(
			UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor2);
		}

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}