package com.example.admin.updatedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String url = "http://download.huotun.com/yf/hnmj.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateUtil.getInstance().downFile(url, MainActivity.this, new UpdateUtil.DownloadCallback() {
            @Override
            public void downloadPrecent(int precent) {
                Log.e("xiaojin","    ---进度---"+precent);
                Toast.makeText(MainActivity.this,"---进度---"+precent,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void downloadFail() {
                Log.e("xiaojin","    ---下载失败---");
                Toast.makeText(MainActivity.this,"下载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void downloadSuccess() {
                Log.e("xiaojin","    ---下载完成,开始安装---");
                Toast.makeText(MainActivity.this,"下载完成,开始安装",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
