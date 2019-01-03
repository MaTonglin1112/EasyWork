package com.example.msi.easywork;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class MyService extends Service {

    private static final String CHANNEL_ID = "com.example.msi.easywork.MyService";
    public static final int NOTIFICATION_ID = 0x234;
    private String description = "description";

    public MyService() {
    }

    @Override
    public void onCreate() {
        System.out.println("com.example.msi.easywork.MyService.onCreate");
        super.onCreate();
    }

    private NotificationManager notificationManager;
    /**
     *通过通知启动服务
     */
    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    public void  setForegroundService() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
        //设定的通知渠道名称
        String channelName = getString(R.string.channel_name);
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(description);
        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(android.R.drawable.btn_star) //设置通知图标
                .setContentTitle("title")//设置通知标题
                .setContentText("notificationContent")//设置通知内容
                //.setAutoCancel(true) //用户触摸时，自动关闭
                .setOngoing(true)
                .setContentIntent(activity);//设置处于运行状态
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID,builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        System.out.println("com.example.msi.easywork.MyService.onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        System.out.println("com.example.msi.easywork.MyService.onLowMemory");
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("com.example.msi.easywork.MyService.onUnbind");
        return super.onUnbind(intent);
    }

    // 返回给客户端的 Binder
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("com.example.msi.easywork.MyService.onBind");
        return mBinder;
    }

    private boolean isRunning = false;
    /**
     * 重新计时使用时间
     */
    public void resetTime() {
        if (isRunning){
            myRunable.reset();
            Toast.makeText(this, "时间重置成功!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "服务还未启动!", Toast.LENGTH_SHORT).show();
        }
    }

    private TextToSpeech textToSpeech;
    public void initTTS(){
        textToSpeech = new TextToSpeech(MyService.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE) {
                        Toast.makeText(MyService.this, "TTS暂时不支持这种语音的朗读！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private InnerRunable myRunable;
    /**
     * 开始他的服务
     */
    public void startSelf() {
        //只启动一次
        if (!isRunning){
            System.out.println("com.example.msi.easywork.MyService.startSelf");
            //如果API在26以上即版本为O则调用startForefround()方法启动服务
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                System.out.println("设置前台服务完成");
                setForegroundService();
            }
            if (textToSpeech == null){
                System.out.println("初始化语音引擎完成!");
                initTTS();
            }
            handler = new Handler();
            myRunable = new InnerRunable();
            new Thread(myRunable).start();
            isRunning = true;
            Toast.makeText(this, "服务启动成功!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "服务已启动!", Toast.LENGTH_SHORT).show();
        }
    }


    private Handler handler;
    /**
     * 停止服务
     */
    public void stopAll() {
        if (isRunning){
            handler.removeCallbacks(myRunable);
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            stopForeground(true);
            stopSelf();
            isRunning = false;
            myRunable = null;
            Toast.makeText(this, "服务已停止!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "服务还未启动!", Toast.LENGTH_SHORT).show();
        }
    }

    class LocalBinder extends Binder {
        MyService getService(){
            return MyService.this;
        }
    }

    private class InnerRunable implements Runnable {

        private int curTime = 0;    //计时因子
        private String mode = "已用时 ";   //语音模板
        private int durTime = 10 * 60 * 1000;   //单位: 分钟
        private int handlerTime = 1000; //定时轮询时间

        /**
         * 重置方法
         */
        void reset(){
            curTime = 0;
            handler.removeCallbacks(myRunable);
            handler.postDelayed(myRunable, handlerTime);
        }

        @Override
        public void run() {
            System.out.println("com.example.msi.easywork.MyService.InnerRunable.running----------" + curTime);
            //------------------------------------------------------------------------
            if (curTime != 0 && curTime % durTime == 0){
            speak(mode + getMinate(curTime));
            }
            curTime += handlerTime;
            //------------------------------------------------------------------------
            handler.postDelayed(this, handlerTime);
        }

        private void speak(String s) {
            textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, "com.example.msi.easywork.MyService.InnerRunable.speak");
        }

        /**
         * 获取分钟
         * @param curTime
         * @return
         */
        private String getMinate(int curTime) {
            int hour = curTime / 3600000;
            int i = (curTime % 3600000) / 60000;
            return hour > 0? hour+"小时" + i + "分钟" : i+"分钟";
        }

    }
}
