package le.bluetooth.example.com.bluetoothble.manager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

/**
 * Created by zhengcf on 2017/7/21.
 */

public class BluetoothLeController {

    private static final String TAG = "BluetoothLeController";

    Context context;

    private BluetoothLeService mBluetoothLeService;

    private BindFailListener failListener;

    private String mDeviceAddress;

    private GattListener gattListener;

    private BluetoothLeController(Context context, GattListener gattListener) {
        this.context = context;
        this.gattListener = gattListener;
    }

    public static BluetoothLeController newInstance(Context context, GattListener gattListener) {
        return new BluetoothLeController(context, gattListener);
    }

    /**
     * 绑定服务
     *
     * @param mDeviceAddress
     */
    public void bindService(String mDeviceAddress, BindFailListener failListener) {
        this.mDeviceAddress = mDeviceAddress;
        this.failListener = failListener;
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        context.bindService(new Intent(context, BluetoothLeService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public void unbindService() {
        context.unregisterReceiver(mGattUpdateReceiver);
        context.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    /**
     * 连接蓝牙地址
     *
     * @param mDeviceAddress
     */
    public boolean connect(String mDeviceAddress) {
        if (mBluetoothLeService != null) {
            this.mDeviceAddress = mDeviceAddress;
            return mBluetoothLeService.connect(mDeviceAddress);
        } else {
            return false;
        }
    }

    /**
     * 断开蓝牙
     */
    public void disconnect() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        mBluetoothLeService.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * 读数据
     *
     * @param characteristic
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        mBluetoothLeService.readCharacteristic(characteristic);
    }

    /**
     * 写数据
     *
     * @param characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
        characteristic.setValue(value);
        mBluetoothLeService.writeCharacteristic(characteristic);
    }

    /**
     * 获取支持的GattServices
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        return mBluetoothLeService.getSupportedGattServices();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                mBluetoothLeService = null;
                context.unregisterReceiver(mGattUpdateReceiver);
                failListener.onFail();
            }
            connect(mDeviceAddress);
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
                gattListener.onConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                gattListener.onDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                gattListener.onServicesDiscovered();
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_WRITE.equals(action)) {
                BluetoothGattCharacteristic characteristic = intent.getParcelableExtra(BluetoothLeService.EXTRA_CHARACTERISTIC);
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);
                gattListener.onWriteGattCharacteristic(characteristic, status);
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_READ.equals(action)) {
                BluetoothGattCharacteristic characteristic = intent.getParcelableExtra(BluetoothLeService.EXTRA_CHARACTERISTIC);
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);
                gattListener.onCharacteristicRead(characteristic, status);
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_CHANGED.equals(action)) {
                BluetoothGattCharacteristic characteristic = intent.getParcelableExtra(BluetoothLeService.EXTRA_CHARACTERISTIC);
                gattListener.onCharacteristicChanged(characteristic);
            } else if (BluetoothLeService.ACTION_GATT_READ_RSSI.equals(action)) {
                int rssi = intent.getIntExtra(BluetoothLeService.EXTRA_RSSI, -1);
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);
                gattListener.onReadRssi(rssi, status);
            }
        }
    };

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_READ);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_CHANGED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_READ_RSSI);
        return intentFilter;
    }


    /**
     * 绑定服务失败
     */
    interface BindFailListener {
        void onFail();
    }

    /**
     * 缺省适配器
     */
    public static class GattAdapter implements GattListener {

        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected");
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "onDisconnected");
        }

        @Override
        public void onServicesDiscovered() {
            Log.d(TAG, "onServicesDiscovered");
        }

        @Override
        public void onWriteGattCharacteristic(BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onWriteGattCharacteristic");
        }

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
        }

        @Override
        public void onReadRssi(int rssi, int status) {
            Log.d(TAG, "onReadRssi");
        }
    }

    /**
     * Gatt状态监听
     */
    interface GattListener {

        void onConnected();

        void onDisconnected();

        void onServicesDiscovered();

        void onWriteGattCharacteristic(BluetoothGattCharacteristic characteristic, int status);

        void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status);

        void onCharacteristicChanged(BluetoothGattCharacteristic characteristic);

        void onReadRssi(int rssi, int status);

    }
}
