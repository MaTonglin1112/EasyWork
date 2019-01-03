package com.example.msi.easywork;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button start = findViewById(R.id.button_start);
        Button reset = findViewById(R.id.button_reset);
        Button end = findViewById(R.id.button_end);

        start.setOnClickListener(this);
        reset.setOnClickListener(this);
        end.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("com.example.msi.easywork.MainActivity.onStart");

        intent = new Intent(this, MyService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        //startService(intent);
    }

    @Override
    protected void onStop() {
        System.out.println("com.example.msi.easywork.MainActivity.onStop");
        // 解除绑定
        if (mBound) {
            unbindService(conn);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        System.out.println("com.example.msi.easywork.MainActivity.onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        System.out.println("com.example.msi.easywork.MainActivity.onResume");
        super.onResume();
    }

    private MyService myService;
    private boolean mBound;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_start:
                //myService.startSelf();
                intent = new Intent(this, MyService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    System.out.println("android.content.ContextWrapper.startForegroundService");
                    startForegroundService(intent);
                }else {
                    System.out.println("android.content.ContextWrapper.startService");
                    startService(intent);
                }


                break;
            case R.id.button_reset:
                myService.resetTime();
                break;
            case R.id.button_end:
                myService.stopAll();
                break;
        }
    }
}
