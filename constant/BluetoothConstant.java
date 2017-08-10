package le.bluetooth.example.com.bluetoothble.constant;

import java.util.UUID;

/**
 * Created by zhengcf on 2017/7/25.
 */

public class BluetoothConstant {

    /**
     * 扫描设备超时时间
     */
    public static final long SCAN_PERIOD = 10000;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
}
