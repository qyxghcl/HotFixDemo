package com.a50647.hotfixdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.a50647.wpermission.PermissionManager;
import com.a50647.wpermission.Permissions;

import java.io.File;

/**
 * 测试类
 *
 * @author wm
 * @date 2019/2/28
 */
public class MainActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        mTextView = findViewById(R.id.text);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Test test = new Test();
                //测试数值2,1
                //bug版 为2-1,则显示1
                //修复版 为2+1,则显示3
                int add = test.add(2, 1);
                String text;
                int i = 3;
                if (add == i){
                    text = "结果: "+ add+" ,这是修复后的";
                }else {
                    text = "结果: "+ add+" ,这是没有修复的";
                }
                mTextView.setText(text);
            }
        });

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdcard = Environment.getExternalStorageDirectory();
                HotFixUtils.loadFixedDex(getApplicationContext(), sdcard);
            }
        });
    }

    /**
     * 由于需要操作文件夹,所以请求读写权限,可以自行选择请求权限方式
     * 需在manifest中同样申请权限
     */
    private void requestPermission() {
        PermissionManager.getDefault().requestPermission(this
                , new PermissionManager.OnRequestPermissionListener() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDeny(boolean b) {

                    }
                }, Permissions.STORAGE);
    }
}
