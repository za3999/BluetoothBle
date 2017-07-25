package le.bluetooth.example.com.bluetoothble;

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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import le.bluetooth.example.com.bluetoothble.bean.BluetoolthConstant;

/**
 * 蓝牙功能辅助类
 * Created by zhengcf on 2017/7/21.
 */

public class BluetoothLeHelper {

    private final static String TAG = BluetoothLeHelper.class.getSimpleName();
    private boolean mScanning;
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeHelper instance;
    private String address;
    private BluetoothGatt mBluetoothGatt;
    private GattAdapter gattListener;
    private int mConnectionState = BluetoolthConstant.STATE_DISCONNECTED;

    private BluetoothLeHelper(Context context) {
        this.context = context.getApplicationContext();
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public synchronized static BluetoothLeHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothLeHelper(context);
        }
        return instance;
    }


    public void setGattListener(GattAdapter gattListener) {
        this.gattListener = gattListener;
    }

    /**
     * 获取连接状态
     *
     * @return
     */
    public int getConnectionState() {
        return mConnectionState;
    }

    /**
     * 是否支持蓝牙ble功能
     *
     * @return
     */
    public boolean isBluetoothLeSupport() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        //检查是否支持BLE
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 蓝牙是否可用
     *
     * @return
     */
    public boolean isBluetoothEnable() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * 是否正在扫描中
     *
     * @return
     */
    public boolean isScanning() {
        return mScanning;
    }

    /**
     * 开始扫描
     *
     * @param mLeScanCallback
     * @param scantPeriod
     */
    public void startLeScan(final ScanCallBackListener mLeScanCallback, long scantPeriod) {
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                stopLeScan(mLeScanCallback);
                mLeScanCallback.onScanTimeout();
            }
        }, scantPeriod);
    }

    /**
     * 结束扫描
     *
     * @param mLeScanCallback
     */
    public void stopLeScan(BluetoothAdapter.LeScanCallback mLeScanCallback) {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        this.address = address;

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (this.address != null && address.equals(this.address)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoolthConstant.STATE_CONNECTING;
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
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        this.address = address;
        mConnectionState = BluetoolthConstant.STATE_CONNECTING;
        return true;
    }

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
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

    }

    /**
     * 释放资源
     */
    public void release() {
        address = "";
        close();
        gattListener = null;
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

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 写数据
     *
     * @param characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (BluetoolthConstant.UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
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

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                mConnectionState = BluetoolthConstant.STATE_CONNECTED;
                if (mBluetoothGatt != null) {
                    gattListener.onConnected();
                }
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.discoverServices();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                mConnectionState = BluetoolthConstant.STATE_DISCONNECTED;
                if (mBluetoothGatt != null) {
                    gattListener.onDisconnected();
                }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS && mBluetoothGatt != null) {
                gattListener.onServicesDiscovered();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicWrite");
            if (mBluetoothGatt != null) {
                gattListener.onCharacteristicWrite(characteristic, status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead");
            if (mBluetoothGatt != null) {
                gattListener.onCharacteristicRead(characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged");
            if (mBluetoothGatt != null) {
                gattListener.onCharacteristicChanged(characteristic);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG, "onReadRemoteRssi");
            if (mBluetoothGatt != null) {
                gattListener.onReadRssi(rssi, status);
            }
        }

    };

    public interface ScanCallBackListener extends BluetoothAdapter.LeScanCallback {

        void onScanTimeout();
    }


    /**
     * 缺省适配器
     */
    public static abstract class GattAdapter {

        public void onConnected() {
            Log.d(TAG, "onConnected");
        }

        public void onDisconnected() {
            Log.d(TAG, "onDisconnected");
        }

        public void onServicesDiscovered() {
            Log.d(TAG, "onServicesDiscovered");
        }

        public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onWriteGattCharacteristic");
        }

        public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead");
        }

        public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
        }

        public void onReadRssi(int rssi, int status) {
            Log.d(TAG, "onReadRssi");
        }
    }
}
