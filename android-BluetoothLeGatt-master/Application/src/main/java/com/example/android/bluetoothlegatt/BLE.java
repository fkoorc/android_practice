package com.lowett.android.ble;

import android.app.Activity;
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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.text.TextUtils;

//import com.fit.android.utils.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Email: fvaryu@qq.com
 */
public class BluetoothLeManager implements BluetoothAdapter.LeScanCallback {
    public static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 3 * 10 * 1000;

    static final int STATE_DISCONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_CONNECTED = 4;
    public static final int STATE_DISCOVER_SERVICES = 5;

    @IntDef(value = {STATE_DISCONNECTED, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTING, STATE_DISCOVER_SERVICES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);


    private static BluetoothLeManager ourInstance = new BluetoothLeManager();

    //    private Context mContext;
    private boolean is_inited = false;

    private android.bluetooth.BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private int mConnectionState;
    private BluetoothGatt mBluetoothGatt;

    private boolean mScanning;
    private Runnable scanRunnable;
    private Handler mHandler;

    private Runnable connectRunnable;

    private OnDataReceivedListener mOnDataReceivedListener;
    // 记得清掉监听 泄漏
    private OnLeScanListener mOnLeScanListener;
    private OnConnectionStateChangeListener mOnConnectionStateChangeListener;

    private int retryCount;

    public static BluetoothLeManager getInstance() {
        return ourInstance;
    }

    private BluetoothLeManager() {
    }

    public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        mOnDataReceivedListener = onDataReceivedListener;
    }

    public interface OnConnectionStateChangeListener {
        void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

        void onConnectTimeout();
    }

    public void setOnConnectionStateChangeListener(OnConnectionStateChangeListener onConnectionStateChangeListener) {
        mOnConnectionStateChangeListener = onConnectionStateChangeListener;
    }

    public interface OnLeScanListener {
        void onLeScan(BluetoothDevice device);
    }

    public interface OnDataReceivedListener {
        void onDataReceived(int heart);
    }

    private void init() {
        if (!is_inited) {
            is_inited = true;
        }
    }

    private boolean initialize(Context context) {
        init();
        if (!is_inited) {
            throw new RuntimeException("请先调用init");
        }

        if (mBluetoothAdapter != null) {
            return true;
        }
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothLeManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        return mBluetoothAdapter != null;
    }

    public boolean isSupportBluetoothLe(Activity activity) {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isSupportBluetooth(Context context) {
        return initialize(context);
    }

    public void enableBluetooth(Activity activity) {
        if (!initialize(activity)) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean isEnabled(Context context) {
        return initialize(context) && mBluetoothAdapter.isEnabled();
    }

    private void initHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

    public void startScan(OnLeScanListener onLeScanListener) {
        initHandler();
        if (!mScanning) {
            if (scanRunnable == null) {
                scanRunnable = new Runnable() {
                    @Override
                    public void run() {
                        stopScan();
                    }
                };
            }

            mHandler.postDelayed(scanRunnable, SCAN_PERIOD);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mBluetoothAdapter.getBluetoothLeScanner().startScan(this);
//            }else {
            mBluetoothAdapter.startLeScan(this);
//            }

            mScanning = true;

            this.mOnLeScanListener = onLeScanListener;

            Logger.i("开始扫描，蓝牙设备");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        final BluetoothDevice tmp = device;
        Logger.i("扫描到的设备, name=" + device.getName() + ",address=" + device.toString());
        if (mOnLeScanListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnLeScanListener.onLeScan(tmp);
                }
            });
        }
    }

    public void stopScan() {
        initHandler();
        mOnLeScanListener = null;
        Logger.i("停止扫描，蓝牙设备");
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(this);
        }

        if (scanRunnable != null) {
            mHandler.removeCallbacks(scanRunnable);
            scanRunnable = null;
        }
    }

    private void removeConnectRunnable() {
        if (connectRunnable != null) {
            mHandler.removeCallbacks(connectRunnable);
            connectRunnable = null;
        }
    }

    private void retry() {
        if (TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (++retryCount < 11 && mConnectionState < STATE_CONNECTED) {
                    reconnect(retryCount);
                    mHandler.postDelayed(this, retryCount * 5 * 1000);
                    Logger.i("蓝牙重试次数=" + retryCount);
                }

            }
        }, 2000);
    }

    private void reconnect(int count) {
        if ((mConnectionState >= STATE_CONNECTING)) {
            return;
        }

        if (connectRunnable == null) {
            connectRunnable = new Runnable() {
                @Override
                public void run() {
                    mConnectionState = STATE_DISCONNECTING;
                    disconnect();
                }
            };
        }
        mHandler.postDelayed(connectRunnable, count * 3 * 1000);


        if (mBluetoothDeviceAddress != null
                && mBluetoothGatt != null) {
            mBluetoothGatt.connect();
            mConnectionState = STATE_CONNECTING;
        }
    }

    public boolean connect(Context context, String address) {
        if (mConnectionState == STATE_CONNECTED) {
            return false;
        }
        if (mBluetoothAdapter == null || TextUtils.isEmpty(address)) {
            return false;
        }
        initHandler();
        if (connectRunnable == null) {
            connectRunnable = new Runnable() {
                @Override
                public void run() {
                    mConnectionState = STATE_DISCONNECTING;
                    disconnect();

                    if (mOnConnectionStateChangeListener != null) {
                        mOnConnectionStateChangeListener.onConnectTimeout();
                    }
                }
            };
        }
        mHandler.postDelayed(connectRunnable, 30 * 1000);

        stopScan();

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.i("BluetoothAdapter not initialized");
            mConnectionState = STATE_DISCONNECTED;
            return;
        }

        mBluetoothGatt.disconnect();

    }

    public void close() {
        disconnect();
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void disconnectNoRetry() {
        mBluetoothDeviceAddress = null;
        close();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                boolean success = mBluetoothGatt.discoverServices();
                Logger.i("Attempting to start service discovery:" +
                        success);
                removeConnectRunnable();

                Logger.i("链接上");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Logger.i("断开链接");

                retry();
            }

            if (mOnConnectionStateChangeListener != null) {
                mOnConnectionStateChangeListener.onConnectionStateChange(gatt, status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Logger.i("发现服务");
                discoverService();
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            mConnectionState = STATE_CONNECTED;
            broadcastUpdate(characteristic);
        }
    };

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    private List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    private void discoverService() {
        if (mConnectionState == STATE_DISCOVER_SERVICES) {
            return;
        }
        mConnectionState = STATE_DISCOVER_SERVICES;
        List<BluetoothGattService> list = getSupportedGattServices();
        /**
         *  BluetoothGattService = 00001800-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a00-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a01-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a04-0000-1000-8000-00805f9b34fb

         BluetoothGattService = 00001801-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a05-0000-1000-8000-00805f9b34fb

         心跳服务
         BluetoothGattService = 0000180d-0000-1000-8000-00805f9b34fb
         心跳特征
         BluetoothGattCharacteristic = 00002a37-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a38-0000-1000-8000-00805f9b34fb

         BluetoothGattService = 0000180f-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a19-0000-1000-8000-00805f9b34fb

         // 设备名字
         BluetoothGattService = 0000180a-0000-1000-8000-00805f9b34fb
         BluetoothGattCharacteristic = 00002a28-0000-1000-8000-00805f9b34fb
         */
        for (BluetoothGattService s : list) {
            if (!SampleGattAttributes.HEART_RATE_SERVICES.equals(s.getUuid().toString())) {
                continue;
            }
            final List<BluetoothGattCharacteristic> l = s.getCharacteristics();
            for (final BluetoothGattCharacteristic bc : l) {
                if (!SampleGattAttributes.HEART_RATE_MEASUREMENT.equals(bc.getUuid().toString())) {
                    continue;
                }
                Logger.i("连接蓝牙 服务成功");
                setCharacteristicNotification(bc, true);
                return;
            }
        }
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {


        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            int heartRate = characteristic.getIntValue(format, 1);
            Logger.i(String.format(Locale.getDefault(), "Received heart rate: %d", heartRate));
            if (mOnDataReceivedListener != null) {
                mOnDataReceivedListener.onDataReceived(heartRate);
            }
        }
//        else {
        // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }
        /**
         * 2、
         */
//        sendBroadcast(intent);
    }

    public boolean isConnected() {
        return mConnectionState == STATE_CONNECTED;
    }

    public String getConnectedAddress() {
        if (!isConnected()) {
            return null;
        }
        return mBluetoothDeviceAddress;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.i("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);

    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.i("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public void clear() {
        mOnLeScanListener = null;
        mOnConnectionStateChangeListener = null;
    }

    public void release() {
//        connectRunnable = null;
//        mHandler = null;
//         ourInstance = null;
    }
}