package com.example.msi.easywork;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

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
        Button test = findViewById(R.id.button_test);
        type = findViewById(R.id.editText2);

        start.setOnClickListener(this);
        reset.setOnClickListener(this);
        end.setOnClickListener(this);
        stop.setOnClickListener(this);
        test.setOnClickListener(this);


        intent = new Intent(this, MyAlarmManagerService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(conn);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start1:
                if (!myAlarmManagerService.getIsRunning()){
                    myAlarmManagerService.setDurTime(Integer.valueOf(type.getText().toString()));
                    type.setFocusable(false);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        startForegroundService(intent);
                    }
                    else{
                        startService(intent);
                    }
                    //startService(intent);
                }

                //myAlarmManagerService.setDurTime(Integer.valueOf(type.getText().toString()));
                //myAlarmManagerService.startSer();
                //type.setFocusable(false);

                break;
            case R.id.button_reset1:
                myAlarmManagerService.resetTime();
                break;
            case R.id.button_end1:
                type.setFocusable(true);
                myAlarmManagerService.stopLoop();
                break;
            case R.id.button_stop1:
                myAlarmManagerService.stopService();
                break;
            case R.id.button_test:
                startHHHH();
                break;
        }
    }


    private boolean issssss = false;
    private Handler handler = new Handler();
    private void startHHHH() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (issssss){
                    System.out.println(SystemClock.elapsedRealtime());
                    handler.postDelayed(this, 500);
                }
            }
        };
        if(!issssss){
            issssss = true;
            handler.postDelayed(runnable, 0);
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        }else {
            issssss = false;
            handler.removeCallbacks(runnable);
            Toast.makeText(this, "hahah", Toast.LENGTH_SHORT).show();
        }
    }


}
