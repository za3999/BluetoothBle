package le.bluetooth.example.com.bluetoothble.constant;

import java.util.UUID;

/**
 * Created by zhengcf on 2017/7/25.
 */

public class BluetoolthConstant {

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_CHARACTERISTIC_WRITE = "com.example.bluetooth.le.ACTION_GATT_CHARACTERISTIC_WRITE";
    public final static String ACTION_GATT_CHARACTERISTIC_READ = "com.example.bluetooth.le.ACTION_GATT_CHARACTERISTIC_READ";
    public final static String ACTION_GATT_CHARACTERISTIC_CHANGED = "com.example.bluetooth.le.ACTION_GATT_CHARACTERISTIC_CHANGED";
    public final static String ACTION_GATT_READ_RSSI = "com.example.bluetooth.le.ACTION_GATT_READ_RSSI";

    public final static String EXTRA_CHARACTERISTIC = "com.example.bluetooth.le.CHARACTERISTIC";
    public final static String EXTRA_RSSI = "com.example.bluetooth.le.EXTRA_RSSI";
    public final static String EXTRA_STATUS = "com.example.bluetooth.le.EXTRA_STATUS";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

}
