package com.cookandroid.oreosample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


public class SocketService extends Service {

    Socket socket2 = null;
    String Ip2 = null;
    int Port2;
    DataInputStream dis2 = null;
    DataOutputStream dos2 = null;
    Connect connect = null;
    Msg msg2 = null;
    public static Context context2;
    int n4,n6;
    float n5;
    String onOff;
    PendingIntent pending;


    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public void onCreate() {
        context2 = this;
        connect = ((MainActivity)MainActivity.context).conn;
        Ip2 = connect.getIP();
        Port2 = connect.getPORT();
       onOff = "1";

        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



            new Thread(){
            public void run(){  // 소켓 시작부분
                try {
                    socket2 = new Socket(Ip2,Port2);
                    dis2 = new DataInputStream(socket2.getInputStream());
                    dos2 = new DataOutputStream(socket2.getOutputStream());

                    dos2.writeUTF(onOff);    //  소켓 시작하면서 1를 서버에 보냄

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while(true) {

                        dos2.writeUTF(onOff);   // 계속 1를 보내면서 데이터를 받아옴

                        byte[] recv_data = new byte[10];  //   여기부터
                        dis2.read(recv_data);
                        String byte_msg = "";

                        for (byte b : recv_data) {
                            char c = (char) b;
                            byte_msg = byte_msg + c;
                        }

                        if (byte_msg == null) {
                            Toast.makeText(getApplicationContext(), "데이터 받기를 실패했습니다", Toast.LENGTH_SHORT).show();
                            // break;
                        }

                        String[] msg_list = byte_msg.split(",");  // 여기까지 데이터 받는 부분

                        n4 = Integer.valueOf(msg_list[0]);    // 디바이스
                        n5 = Float.valueOf(msg_list[1]);      // 온도
                        n6 = Integer.valueOf(msg_list[2]);    // 습도

                        msg2 = new Msg(n4, n5, n6);         // 메시지 객체


                        ((MainActivity) MainActivity.context).myTextView();

                        if(n6 >= 65){myAlram();}  // 습도가 65이상이 되면 알람

                        onOff = ((MainActivity)MainActivity.context).onOff;  // 메인에 onOff 변수값을 가져와서 중지를 누르면 2로 설정됨

                        if(onOff.equals("2")){   // 중지를 누르면 if에 진입
                            dos2.writeUTF(onOff);   // 서버에 2를 보내서 중지를 요청함
                            try {
                                if(socket2 != null)socket2.close();
                                if(dis2 != null) dis2.close();
                                if(dos2 != null) dos2.close();
                                break;    // 소켓들을 닫으면서 루프에 빠져나옴
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    } stopSelf();  // 루프에 빠져나오면 스스로 서비스를 끔 , 즉 중지버튼을 누르면 소켓을 닫음, 누르지 않으면 연결이 안꺼짐, 알람 설정을 위해서 이렇게함

                } catch (IOException e) {
                    e.printStackTrace();}
            }
        }.start();


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {


        super.onDestroy();
    }


    public void myAlram(){ // 알람객체
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
      //  pending = PendingIntent.getActivity(
      //          SocketService.this,
      //          0, // 보통 default값 0을 삽입
     //           new Intent(getApplicationContext(),MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
     //   );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String chID = "channel_01";
            String chNA = "스마트 벽체 앱";

            NotificationChannel channel = new NotificationChannel(chID, chNA, NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), chID);

        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), null);
        }

        builder.setSmallIcon(R.drawable.ic_noti_icon);
       // builder.setContentTitle("경고");
        builder.setContentText("습도가 너무 높습니다");
        //builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);
        //builder.setOngoing(true);
        //builder.setSound();
        builder.setContentIntent(pending);
        builder.setVibrate(new long[] {0,500});

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.warnnig);
        builder.setLargeIcon(bm);

        Notification notification = builder.build();
        notificationManager.notify(1, notification);



    }





}
