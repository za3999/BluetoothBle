package le.bluetooth.example.com.bluetoothble.bean;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Created by zhengcf on 2017/7/25.
 */

public class BluetoothBean implements Serializable {

    int rssi;
    byte[] scanRecord;
    BluetoothDevice bluetoothDevice;

    public BluetoothBean(int rssi, byte[] scanRecord, BluetoothDevice bluetoothDevice) {
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
