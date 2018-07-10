package fafa.com.visitor.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zhou Bing
 * <p>
 * 2017/10/19.
 */
public class UsbPrintUtil {

    private Context context;
    private UsbDevice device;
    private static UsbManager mUsbManager;
    private static PendingIntent mPermissionIntent;
    private static UsbDeviceConnection mConnection;
    private static UsbEndpoint mEndpointIntr;

    public UsbPrintUtil(Context context) {
        this.context = context;
    }

    /**
     * 打印设备是否打开成功
     *
     * @return
     */
    private boolean openUsbDevice() {
        //before open usb device
        //should try to get usb permission
        tryGetUsbPermission();

        UsbInterface intf = null;
        UsbEndpoint ep = null;

        int InterfaceCount = device.getInterfaceCount();
        int j;

        for (j = 0; j < InterfaceCount; j++) {
            int i;

            intf = device.getInterface(j);
            Log.i("test", "接口是:" + j + "类是:" + intf.getInterfaceClass());
            if (intf.getInterfaceClass() == 7) {
                int UsbEndpointCount = intf.getEndpointCount();
                for (i = 0; i < UsbEndpointCount; i++) {
                    ep = intf.getEndpoint(i);
                    Log.i("test", "端点是:" + i + "方向是:" + ep.getDirection() + "类型是:" + ep.getType());
                    if (ep.getDirection() == 0 && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        Log.i("test", "接口是:" + j + "端点是:" + i);
                        break;
                    }
                }
                if (i != UsbEndpointCount) {
                    break;
                }
            }
        }
        if (j == InterfaceCount) {
            Log.i("test", "没有打印机接口");
            return false;
        }

        mEndpointIntr = ep;
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        if (connection != null && connection.claimInterface(intf, true)) {
            Log.i("test", "打开成功！ ");
            mConnection = connection;
            return true;
        } else {
            Log.i("test", "打开失败！ ");
            mConnection = null;
            return false;
        }
    }

    public void print(final Map<String, Object> parameters) {
        boolean flag = openUsbDevice();
        usbPrint(parameters);
        close();
        if (!flag) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    openUsbDevice();
                    usbPrint(parameters);
                    close();
                }
            }, 5000);
        }
    }

    private void usbPrint(Map<String, Object> parameters) {
//        sendCommand(mEndpointIntr, mConnection, PrinterCmdUtils.init_printer());
//        StringBuilder data = new StringBuilder();
//        //设置纸张尺寸，标签间隔距离、标签宽度、标签高度
//        data.append("{D1000,0900,0600|}");
//        data.append("{C|}");
//        data.append("{LC;0000,0129,0899,0129,0,3|}");
//        data.append("{PC000;0010,0089,15,15,r,00,B=GeneralMills|}");
//        data.append("{PC001;0590,0086,1,1,r,00,B=").append(parameters.get("visitTime").toString(), 0, 10).append("|}");
//        data.append("{PC001;0900,0300,15,15,r,22,B|}");
//        data.append("{PC002;0020,0229,15,15,r,00,B=Name:|}");
//        data.append("{PC003;0020,0309,15,15,r,00,B=Company:|}");
//        data.append("{PC004;0020,0389,15,15,r,00,B=Visiting:|}");
//        data.append("{PC005;0020,0469,15,15,r,00,B=Dept.:|}");
//        data.append("{PC006;0020,0559,15,15,r,00,B=PinCode:|}");
//        data.append("{PC007;0450,0229,15,15,r,00,B=").append(parameters.get("visitName")).append("|}");
//        data.append("{PC008;0450,0309,15,15,r,00,B=").append(parameters.get("visitCompany")).append("|}");
//        data.append("{PC009;0460,0389,15,15,r,00,B=").append(parameters.get("name")).append("|}");
//        data.append("{PC010;0460,0469,15,15,r,00,B=").append(parameters.get("department")).append("|}");
//        data.append("{PC011;0460,0559,15,15,r,00,B=").append(parameters.get("pinCode")).append("|}");
//        data.append("{XS;I,0001,0000C5001|}");
//        try {
//            sendCommand(mEndpointIntr, mConnection, data.toString().getBytes("GB2312"));
//            context.unregisterReceiver(mUsbPermissionActionReceiver);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        sendCommand(mEndpointIntr, mConnection, PrinterCmdUtils.init_printer());
        StringBuilder data = new StringBuilder();
        //设置纸张尺寸，标签间隔距离、标签宽度、标签高度
        data.append("{D1000,0900,0600|}");
        //清除缓存区数据
        data.append("{C|}");
        //向后走纸到打印位置
        data.append("{U2;0030|}");
        //定位打印原点x
        data.append("{AX;+000,+000,+00|}");
        //定位打印原点y
        data.append("{AY;+00,0|}");
        data.append("{PC001;0900,0480,15,15,r,22,B|}");
        String s = parameters.get("visitTime")+"";
        if(s.length()>11){
            s = s.substring(0,10);
        }
        //打印数据
        data.append("{RC001; General Mills           " + s + "|}");
        data.append("{PC002;0900,0410,15,15,r,22,B|}");
        data.append("{RC002; ......................................|}");
        //打印数据格式
        data.append("{PC003;0900,0340,15,15,r,22,B|}");
        //打印数据
        data.append("{RC003; Name:          " + parameters.get("visitName") + "|}");
        //打印数据格式
        data.append("{PC004;0900,0270,15,15,r,22,B|}");
        //打印数据
        data.append("{RC004; Company:       " + parameters.get("visitCompany") + "|}");
        //打印数据格式
        data.append("{PC005;0900,0200,15,15,r,22,B|}");
        //打印数据
        data.append("{RC005; Visiting:      " + parameters.get("name") + "|}");
        //打印数据格式
        data.append("{PC006;0900,0130,15,15,r,22,B|}");
        //打印数据
        data.append("{RC006; Dept.:         " + parameters.get("department") + "|}");
        //打印数据格式
        data.append("{PC007;0900,0060,15,15,r,22,B|}");
        //打印数据
        data.append("{RC007; PinCode:       " + parameters.get("pinCode") + "|}");
//        data.append("{LC;0880,0400,0020,0400,0,3|}");
        //打印设置
        data.append("{XS;I,0001,0000C1010|}");
        try {
            sendCommand(mEndpointIntr, mConnection, data.toString().getBytes("GB2312"));
            context.unregisterReceiver(mUsbPermissionActionReceiver);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void sendCommand(UsbEndpoint mEndpointIntr, UsbDeviceConnection mConnection, byte[] content) {
        synchronized (this) {
            int len = -1;
            if (mConnection != null) {
                len = mConnection.bulkTransfer(mEndpointIntr, content, content.length, 10000);
            }

            if (len < 0) {
                System.out.println("发送失败！ " + len);
            } else {
                System.out.println("发送" + len + "字节数据");
            }
        }
    }

    private void close() {
        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }
        if (mPermissionIntent != null) {
            mPermissionIntent.cancel();
            mPermissionIntent = null;
        }
        if (mEndpointIntr != null) {
            mEndpointIntr = null;
        }
        mUsbManager = null;
        System.out.println("关闭");
    }

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private void tryGetUsbPermission() {
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbPermissionActionReceiver, filter);

        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //here do emulation to ask all connected usb device for permission
        Collection<UsbDevice> collections = mUsbManager.getDeviceList().values();
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            if (usbDevice.getVendorId() == 2214) {
                device = usbDevice;
            }
            System.out.println(usbDevice.getVendorId() + " if has permission " + mUsbManager.hasPermission(usbDevice));
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
            //}
        }
    }


    private void afterGetUsbPermission(UsbDevice usbDevice) {
        //call method to set up device communication
        System.out.println(String.valueOf("Got permission for usb device: " + usbDevice));
        System.out.println(String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()));

        doYourOpenUsbDevice(usbDevice);
    }

    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        //add your operation code here

        System.out.println(usbDevice.getVendorId() + "~~~~~~~~~~~~~");
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        System.out.println(String.valueOf("Permission denied for device" + usbDevice));
                    }
                }
            }
        }
    };
}
