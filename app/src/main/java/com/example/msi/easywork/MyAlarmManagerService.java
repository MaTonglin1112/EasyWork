package com.example.msi.easywork;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Locale;

public class MyAlarmManagerService extends Service {
    private static final int NOTIFICATION_ID = 0x1;
    private static final String CHANNEL_ID = "com.example.msi.easywork.MyAlarmManagerService";
    private MyBinder myBinder = new MyBinder();

    private int curTime = 0;    //计时因子
    private String mode = "已用时 ";   //语音模板
    private int durTime = 2 * 60 * 1000;   //单位: 分钟
    private int handlerTime = 5000; //定时轮询时间
    private boolean isScreenOn = false;

    private AlarmManager manager;
    private PendingIntent pi;
    private NotificationCompat.Builder builder;

    private TextToSpeech textToSpeech;
    private boolean isRunning = false;

    public MyAlarmManagerService() {
    }

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int i = textToSpeech.setLanguage(Locale.CHINA);
                    if (i != TextToSpeech.LANG_COUNTRY_AVAILABLE && i != TextToSpeech.LANG_AVAILABLE) {
                        Toast.makeText(MyAlarmManagerService.this, "暂时不支持这种语言", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        pi = PendingIntent.getBroadcast(this, 0, i, 0);

        Intent intent = new Intent(this, MyAlarmManagerService.class);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
        //设定的通知渠道名称
        String channelName = getString(R.string.channel_name);
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //设置通知的重要程度
            int importance = NotificationManager.IMPORTANCE_LOW;
            //构建通知渠道
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        //在创建的通知渠道上发送通知
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("初始化成功")
                .setSmallIcon(android.R.drawable.btn_star)
                .setOngoing(true)
                .setSound(null)
                .setContentIntent(activity)
                .setContentTitle("工作中...");

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("com.example.msi.easywork.MyService.InnerRunable.running----------" + curTime);
                if (curTime != 0 && curTime % durTime == 0) {
                    speak(mode + getMinate(curTime));
                }
                PowerManager systemService = (PowerManager) getSystemService(POWER_SERVICE);
                assert systemService != null;
                isScreenOn = systemService.isInteractive();
                System.out.println("isScreenOn + " + isScreenOn);
                if (isScreenOn) {
                    builder.setContentText(getHourMinite(curTime));
                    //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
                    startForeground(NOTIFICATION_ID, builder.build());
                }
                curTime += handlerTime;
            }
        }).start();

        long triggerAtTime = SystemClock.elapsedRealtime() + handlerTime;
        manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("com.example.msi.easywork.MyAlarmManagerService.onDestroy");
        textToSpeech.stop();
        textToSpeech.shutdown();
        super.onDestroy();
    }

    /**
     * 启动服务
     */
    public void startSer() {
        if (isRunning) {
            Toast.makeText(this, "服务已启动!", Toast.LENGTH_SHORT).show();
        } else {
            isRunning = true;
            long triggerAtTime = SystemClock.elapsedRealtime() + 1000;
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, handlerTime, pi);
            Toast.makeText(this, "服务启动!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 停止服务
     */
    public void stopLoop() {
        if (isRunning) {
            manager.cancel(pi);
            isRunning = false;
            Toast.makeText(this, "服务已停止!", Toast.LENGTH_SHORT).show();
        } else {
            isRunning = true;
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pi);
            Toast.makeText(this, "服务继续运行!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 终止后台服务
     */
    public void stopService() {
        manager.cancel(pi);
        isRunning = false;
        stopForeground(true);
        Toast.makeText(this, "服务已清除!", Toast.LENGTH_SHORT).show();
    }


    /**
     * 重新计时使用时间
     */
    public void resetTime() {
        if (isRunning) {
            curTime = 0;
            Toast.makeText(this, "时间重置成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "服务还未启动!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    class MyBinder extends Binder {
        MyAlarmManagerService getService() {
            return MyAlarmManagerService.this;
        }
    }

    private String getHourMinite(int curTime) {
        int hour = curTime / 3600000;
        int tmp = curTime % 3600000;
        int min = (tmp) / 60000;
        int second = (tmp % 60000) / 1000;
        return hour + ":" + min + ":" + second;
    }

    private void speak(String s) {
        textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, "com.example.msi.easywork.MyService.InnerRunable.speak");
    }

    /**
     * 获取分钟
     *
     * @param curTime
     * @return
     */
    private String getMinate(int curTime) {
        int hour = curTime / 3600000;
        int i = (curTime % 3600000) / 60000;
        return hour > 0 ? hour + "小时" + (i == 0 ? "" : i + "分钟") : i + "分钟";
    }

    public void setDurTime(Integer integer) {
        durTime = integer * 60 * 1000;
    }
}
