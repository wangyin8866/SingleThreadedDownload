package com.wy.multithreadingdownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wy.multithreadingdownload.entity.FileInfo;
import com.wy.multithreadingdownload.service.DownloadService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTvFileName;
    private ProgressBar mPBar;
    private Button mBtStar, mBtStop;
    private String url = "http://p.gdown.baidu.com/b7cb15c41f7e66daede74b013a00a246224112cee28f139d13929c8d5b687088d6102c9d72534e534a11e7d8ada414f46b1d21e718fd2bf912adac10204be75c23a07ac42d6dd78adf4eac36fcc5b325052aecd162c412c67b38e0dc6cf5063867a10d1375af4633392380e7cfa471883b33669ccf1d49a513809f8c37646b6b9d1c610fea78ff5251cc99d4e64018ddba7a37e2d9610c11eda7a7e5d43bd2098b941575997a782698efdac8bbd3aa4a2cdb2c8182b359c7ee11762bb37d2b64d5086aa20265a3d0ab4524a5339fdcf9f2b284913cde73de7c3f3b2b1c24e465";
    private FileInfo fileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //创建文件信息对象
        fileInfo = new FileInfo(0, url, "KuWo.apk", 0, 0);

        mBtStar.setOnClickListener(this);
        mBtStop.setOnClickListener(this);
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(receiver, filter);
    }

    private void initView() {
        mTvFileName = (TextView) findViewById(R.id.tvFileName);
        mPBar = (ProgressBar) findViewById(R.id.pbFile);
        mBtStar = (Button) findViewById(R.id.btStar);
        mBtStop = (Button) findViewById(R.id.btStop);
        mPBar.setMax(100);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btStar:
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
                break;
            case R.id.btStop:
                Intent intent2 = new Intent(MainActivity.this, DownloadService.class);
                intent2.setAction(DownloadService.ACTION_STOP);
                intent2.putExtra("fileInfo", fileInfo);
                startService(intent2);
                break;
        }
    }

    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                mPBar.setProgress(finished);
            }
        }
    };
}
