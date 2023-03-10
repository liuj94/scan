package com.scan.demo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hello.scan.ScanCallBack;
import com.hello.scan.ScanTool;
import com.scan.demo.databinding.ActivityMainBinding;
import com.telpo.tps550.api.util.SystemUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ScanCallBack {

    private static final Map<String, Pair<String, Integer>> sDevices;

    static {
        /*
         * 支持设备列表
         */
        sDevices = new HashMap<>();
        sDevices.put("TPS508", new Pair<>("/dev/ttyACM0", 115200));
        sDevices.put("TPS360", new Pair<>("/dev/ttyACM0", 115200));
        sDevices.put("TPS537", new Pair<>("/dev/ttyACM0", 115200));
        sDevices.put("D2", new Pair<>("/dev/ttyHSL0", 9600)); // D2串口模式
        //sDevices.put("D2", new Pair<>("/dev/ttyACM0", 115200)); // D2U转串模式
        sDevices.put("TPS980", new Pair<>("/dev/ttyS0", 115200));
        sDevices.put("TPS980P", new Pair<>("/dev/ttyS0", 115200));
        //sDevices.put("TPS980P", new Pair<>("/dev/ttyS0", 9600));
        sDevices.put("TPS530", new Pair<>("/dev/ttyUSB0", 115200));
    }

    private int scanCount = 0;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        SystemUtil.getInternalModel();
        mBinding.scanResult.setText("SystemUtil.getInternalModel()=="+SystemUtil.getInternalModel());
//        Toast.makeText(this, "SystemUtil.getInternalModel()=="+SystemUtil.getInternalModel(), Toast.LENGTH_SHORT).show();
        if (!initScanTool()) {
            Toast.makeText(this, "该机型还没有适配", Toast.LENGTH_SHORT).show();
        } else {
            ScanTool.GET.playSound(true);
        }

        mBinding.init.setOnClickListener(pView -> initScanTool());
        mBinding.release.setOnClickListener(pView -> ScanTool.GET.release());
        mBinding.pauseReceiveData.setOnClickListener((pView -> ScanTool.GET.pauseReceiveData()));
        mBinding.resumeReceiveData.setOnClickListener((pView -> ScanTool.GET.resumeReceiveData()));
    }

    /**
     * 判断使用模式
     *
     * @return 返回true表示表示该机型已经适配，false表示该机型还没有适配
     */
    private boolean initScanTool() {
        for (String s : sDevices.keySet()) {
            if (s.equals(Build.MODEL)) {
                Pair<String, Integer> pair = sDevices.get(s);
                if (pair == null) continue;
                Log.e("Hello", "judgeModel == > " + s);
                Log.e("Hello", "path == > " + pair.first);
                Log.e("Hello", "baud rate == > " + pair.second);
                ScanTool.GET.initSerial(this, pair.first, pair.second, MainActivity.this);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onScanCallBack(String data) {
        if (TextUtils.isEmpty(data)) return;
        Log.e("Hello", "回调数据 == > " + data);
        if (scanCount % 2 == 0) {
            mBinding.scanResult.setTextColor(Color.RED);
        } else {
            mBinding.scanResult.setTextColor(Color.BLUE);
        }
        scanCount++;
        mBinding.scanResult.setText(String.format(Locale.CHINA, "扫码结果：\n\n%s", data));
        mBinding.tips.setText(String.format(Locale.CHINA, "扫码次数：%d次", scanCount));
    }

    @Override
    public void onInitScan(boolean isSuccess) {
        String str = isSuccess ? "初始化成功" : "初始化失败";
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScanTool.GET.release();
    }
}