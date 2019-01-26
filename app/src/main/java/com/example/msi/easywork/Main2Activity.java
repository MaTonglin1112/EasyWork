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
import android.widget.EditText;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private EditText type;
    private MyAlarmManagerService myAlarmManagerService;
    private Intent intent;
    private boolean mBound = false;

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyAlarmManagerService.MyBinder myBinder = (MyAlarmManagerService.MyBinder) service;
            myAlarmManagerService = myBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button start = findViewById(R.id.button_start1);
        Button reset = findViewById(R.id.button_reset1);
        Button end = findViewById(R.id.button_end1);
        Button stop = findViewById(R.id.button_stop1);
        type = findViewById(R.id.editText2);

        start.setOnClickListener(this);
        reset.setOnClickListener(this);
        end.setOnClickListener(this);
        stop.setOnClickListener(this);


        intent = new Intent(this, MyAlarmManagerService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        System.out.println("com.example.msi.easywork.Main2Activity.onStop");
        if (mBound) {
            unbindService(conn);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        System.out.println("com.example.msi.easywork.Main2Activity.onDestroy");
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start1:
                if (!myAlarmManagerService.getIsRunning()){
                    myAlarmManagerService.setDurTime(Integer.valueOf(type.getText().toString()));
                    type.setFocusable(false);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        System.out.println("android.content.ContextWrapper.startForegroundService");
                        startForegroundService(intent);
                    }
                    Toast.makeText(this, "服务启动!", Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(this, "服务已启动!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_reset1:
                myAlarmManagerService.resetTime();
                break;
            case R.id.button_end1:
                myAlarmManagerService.stopLoop();
                break;
            case R.id.button_stop1:
                type.setFocusable(true);
                myAlarmManagerService.stopService();
                break;
        }
    }

}
