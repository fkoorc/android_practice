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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import org.apache.http.util.EncodingUtils;

import com.wearable_test.myapplication.PullDownListView.ListViewTouchEventListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends Activity implements OnInitListener{
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    public ProgressDialog myDialog = null;
    private Handler mHandler;
    PullDownListView scanlist;
    //private ImageButton scanbtn;
    //int i=0,sel_count=0;
    String bestmac="";
    int rssi;
    private TextToSpeech mSpeech = null;
    String fileName = "Wearable_connect_mac.txt";
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        scanlist = (PullDownListView)findViewById(R.id.lstData);
        mHandler = new Handler();
    	try {
    	   	 FileInputStream fileIn=openFileInput(fileName);
    	   	 InputStreamReader InputRead= new InputStreamReader(fileIn);
    	   	 
    	   	 char[] inputBuffer= new char[100];
    	   	 bestmac="";
    	   	 int charRead;
    	   	 
    	   	 while ((charRead=InputRead.read(inputBuffer))>0) {
    	   	 // char to string conversion
    	   	 String readstring=String.copyValueOf(inputBuffer,0,charRead);
    	   	 bestmac +=readstring; 
    	   	 }
    	   	 InputRead.close();
    	       bestmac=bestmac.toString();
    	   } 
    	catch (IOException e) {
    	       e.printStackTrace();
    	       bestmac = "";
    	   }
    	    
        Log.i("bestmac",bestmac+"N");
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
           // finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        rssi = this.rssi ;
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
            
        }
        scanlist.setAdapter(mLeDeviceListAdapter);
        scanlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                //final Intent intent = new Intent(this, DeviceControlActivity.class);
                final Intent intent = new Intent();
                intent.setClass(DeviceScanActivity.this, Menu_page.class);     
                startActivity(intent);
                FileOutputStream fout;
                
           //     if(sel_count>=1){
           //     	sel_count=0;
        		try {
        			fout = openFileOutput(fileName, Context.MODE_PRIVATE);
        			OutputStreamWriter outputWriter=new OutputStreamWriter(fout);
           	 try {
        		outputWriter.write(device.getAddress());
        		Log.w("mac",device.getAddress() );
        		outputWriter.close();
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
           	 
        		} catch (FileNotFoundException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}	
                //intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(DeviceControlA_distence.EXTRAS_DEVICE_NAME, "Best lab");       
                intent.putExtra(DeviceControlA_distence.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
             //   utts.say("裝置選擇完成");
                startActivity(intent);
         //       }
//                else{
         //       	sel_count=+1;
          //      	utts.say("選擇裝置"+i+1);
          //      }
            
				
			}
			
        });

        scanlist.setListViewTouchListener(new ListViewTouchEventListener(){

        	   @Override
        	   public void onListViewPulledDown() {
        	    // TODO Auto-generated method stub
        		   mLeDeviceListAdapter.clear();
                   scanLeDevice(true);
        	   }
        	         
        	        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                onBackPressed();
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        scanlist.setAdapter(mLeDeviceListAdapter);
        //getListView().setBackgroundColor(Color.parseColor("#ECECDA"));
         
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }
 

 
    public void onInit (int status){

        //what you want to do just after the completion of the TextToSpeech engine initialization
        }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            if(bestmac.equals(""))
            {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
        	
        	final CharSequence strDialogTitle =
                 		getString(R.string.scanning_title);
            final CharSequence strDialogbody =
                 		getString(R.string.scanning_body);
         /*        myDialog = ProgressDialog.show(
                 		DeviceScanActivity.this,
                 		strDialogTitle,
                 		strDialogbody,
                 		true);*/
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
               viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
         //       scanbtn = (ImageButton)view.findViewById(R.id.scanbtn);
                view.setTag(viewHolder);
         /*       scanbtn.setOnClickListener(new Button.OnClickListener(){
                        @Override
                     public void onClick(View v){
                        scanLeDevice(true);
                    }
                    });*/
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
        //    if(i>=1)
        //   	scanbtn.setVisibility(view.GONE);
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (device.getAddress().equals(bestmac) )
            {
            	mScanning = false;
            	Intent intent = new Intent(DeviceScanActivity.this,Menu_page.class);
            	intent.putExtra(Menu_page.EXTRAS_DEVICE_NAME, device.getName()); 
                intent.putExtra(Menu_page.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            	startActivity(intent);//開始跳往要去的Activity}
            }
            else{
            	if(bestmac.equals("")){
            //final String deviceName ="Best Lab";
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
              viewHolder.deviceAddress.setText(device.getAddress());
             }
            	else{
                	final Intent intent2 = new Intent(DeviceScanActivity.this,DeviceScanActivity.class);
                	mBluetoothAdapter.stopLeScan(mLeScanCallback);
                	new AlertDialog.Builder(DeviceScanActivity.this)
                	.setTitle(R.string.scan_nothing_title)
                	.setMessage(R.string.scan_nothing_body)
                	.setCancelable(false)
                	.setPositiveButton("再搜尋",
                			new DialogInterface.OnClickListener()
                	{
                		public void onClick(DialogInterface dialoginterface,int i)
                		{                	
                			mBluetoothAdapter.stopLeScan(mLeScanCallback);
                	           startActivity(intent2);
                		}
                	})
                	.setNegativeButton("新設定",
                			new DialogInterface.OnClickListener()
                	{
                		public void onClick(DialogInterface dialoginterface,int i)
                		{                	
                			mBluetoothAdapter.stopLeScan(mLeScanCallback);
                			getApplicationContext().deleteFile(fileName);
                			 startActivity(intent2);
                		}
                	}).show();
                	
            	}}
            return view;
        }
    }

    	
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}