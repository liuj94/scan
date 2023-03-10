package com.scan.demo;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import com.hello.utils.IoUtil;
import com.hello.worker.DataPostman;
import com.hello.worker.ScanToolImpl;
import com.hello.worker.SoundPlayer;

public class ScanService extends Service {
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra("device");
            if (usbDevice != null) {
                String productName = usbDevice.getProductName();
                boolean m1 = productName.contains("NLS-EM23");
                boolean m2 = productName.contains("NLS-CEM300-DK");
                boolean m3 = productName.contains("NLS-FM80-U");
                boolean m4 = productName.contains("WCM COM");
                if ((m1 || m2 || m3 || m4) && ScanToolImpl.getInstance().init()) {
                    ScanToolImpl.getInstance().startReceiveData();
                }

            }
        }
    };

    public ScanService() {
    }

    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        this.registerReceiver(this.mBroadcastReceiver, filter);
        DataPostman.getInstance().init(this);
        SoundPlayer.getInstance().init(this);
        Log.v("Hello", "ScanService onCreate == > call");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String cmd = intent.getStringExtra("cmd");
        Bundle bundle = intent.getBundleExtra("data");
        if (!TextUtils.isEmpty(cmd)) {
            switch (cmd) {
                case "resume_receive_data":
                    ScanToolImpl.getInstance().startReceiveData();
                    break;
                case "pause_receive_data":
                    ScanToolImpl.getInstance().stopReceiveData();
                    break;
                case "play_sound":
                    boolean play = bundle.getBoolean("play", false);
                    ScanToolImpl.getInstance().playSound(play);
                    break;
                case "init_serial":
                    IoUtil.turnOn();
//                    if (ScanToolImpl.getInstance().isCdcModel()) {
//                        Log.e("Hello", "onStartCommand == > CDC模式");
////                        IoUtil.turnOff();
////                        SystemClock.sleep(10L);
//                        IoUtil.turnOn();
//                    } else {
//                        Log.e("Hello", "onStartCommand == > 串口模式");
//                        if (ScanToolImpl.getInstance().init()) {
//                            ScanToolImpl.getInstance().startReceiveData();
//                        }
//                    }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        this.unregisterReceiver(this.mBroadcastReceiver);
        ScanToolImpl.getInstance().release();
        DataPostman.getInstance().release();
        SoundPlayer.getInstance().release();
        IoUtil.turnOff();
        Log.v("Hello", "ScanService onDestroy == > call");
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}
