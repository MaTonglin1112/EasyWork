package com.example.msi.easywork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myService = new Intent(context, MyAlarmManagerService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            context.startForegroundService(myService);
        }
        else{
            context.startService(myService);
        }
        //context.startService(myService);
    }
}
