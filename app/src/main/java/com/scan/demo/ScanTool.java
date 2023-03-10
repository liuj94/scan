package com.scan.demo;
 

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hello.scan.ScanCallBack;

import com.hello.worker.ScanToolImpl;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

public class ScanTool {
    public static ScanTool GET = new ScanTool();
    private ScanCallBack mScanCallBack;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("cmd");
            if (!TextUtils.isEmpty(cmd)) {
                Bundle bundle = intent.getBundleExtra("data");
                switch (cmd) {
                    case "init_serial":
                        boolean aBoolean = bundle.getBoolean("init_result", false);
                        if (ScanTool.this.mScanCallBack != null) {
                            ScanTool.this.mScanCallBack.onInitScan(aBoolean);
                        }
                        break;
                    case "scan_data_return":
                        byte[] byteArray = bundle.getByteArray("scan_data");
                        if (byteArray == null || byteArray.length == 0) {
                            return;
                        }

                        String data = new String(byteArray, StandardCharsets.UTF_8);

                        for(int i = 0; i < 2; ++i) {
                            if (data.endsWith("\r") || data.endsWith("\n")) {
                                data = data.substring(0, data.length() - 1);
                            }
                        }

                        if (ScanTool.this.mScanCallBack != null) {
                            ScanTool.this.mScanCallBack.onScanCallBack(byteArray);
                            ScanTool.this.mScanCallBack.onScanCallBack(data);
                        }
                }

            }
        }
    };
    private WeakReference<Context> mContext;
    private Intent mIntent;

    private ScanTool() {
    }

    public void initSerial(Context pContext, String pPath, int pBaudRate) {
        this.initSerial(pContext, pPath, pBaudRate, this.mScanCallBack);
    }

    public void initSerial(Context pContext, String pPath, int pBaudRate, ScanCallBack pScanCallBack) {
        this.mScanCallBack = pScanCallBack;
        this.mContext = new WeakReference(pContext.getApplicationContext());
        IntentFilter filter = new IntentFilter("scan_tool_action");
        LocalBroadcastManager.getInstance((Context)this.mContext.get()).registerReceiver(this.mBroadcastReceiver, filter);
        ScanToolImpl.PATH = pPath;
        ScanToolImpl.BAUD_RATE = pBaudRate;
        this.sendCmd("init_serial");
    }

    public void setScanCallBack(ScanCallBack pScanCallBack) {
        this.mScanCallBack = pScanCallBack;
    }

    public void resumeReceiveData() {
        this.sendCmd("resume_receive_data");
    }

    public void pauseReceiveData() {
        this.sendCmd("pause_receive_data");
    }

    public void playSound(boolean play) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("play", play);
        this.sendCmd("play_sound", bundle);
    }

    public void release() {
        this.mScanCallBack = null;
        LocalBroadcastManager.getInstance((Context)this.mContext.get()).unregisterReceiver(this.mBroadcastReceiver);
        if (this.mContext != null) {
            ((Context)this.mContext.get()).stopService(new Intent((Context)this.mContext.get(), ScanService.class));
        }

    }

    private synchronized void sendCmd(String pCmd) {
        if (this.mIntent == null) {
            this.mIntent = new Intent((Context)this.mContext.get(), ScanService.class);
        }

        this.mIntent.putExtra("cmd", pCmd);
        ((Context)this.mContext.get()).startService(this.mIntent);
    }

    private synchronized void sendCmd(String pCmd, Bundle pData) {
        if (this.mIntent == null) {
            this.mIntent = new Intent((Context)this.mContext.get(), ScanService.class);
        }

        this.mIntent.putExtra("cmd", pCmd);
        if (pData != null) {
            this.mIntent.putExtra("data", pData);
        }

        ((Context)this.mContext.get()).startService(this.mIntent);
    }
}

