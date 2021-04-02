package com.cookandroid.oreosample;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.CalendarView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Calendar extends Activity{

    CalendarView cal;
    int selectMonth;
    DataOutputStream dos = null;
    DataInputStream dis = null;
    Socket socket = null;
    String Ip;
    int Port;
    Connect connect = null;
    String s = null;
    String ms = null;
    String ds = null;
    Msg msg;
    TextView txt1,txt2,txt3,txt4;
    int n4,n6;
    float n5;
    ProgressDialog Pro;
    int rock;
    int txtRock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        cal = (CalendarView) findViewById(R.id.cal);
        txt1 = (TextView) findViewById(R.id.calTxt1);
        txt2 = (TextView) findViewById(R.id.calTxt2);
        txt3 = (TextView) findViewById(R.id.calTxt3);
        txt4 = (TextView) findViewById(R.id.calTxt4);

        connect = ((MainActivity)MainActivity.context).conn;
        Ip = connect.getIP();
        Port = connect.getPORT();
        rock = 0;
        txtRock = 1;

        Pro = new ProgressDialog(this);  // 로딩화면 객체
        Pro.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Pro.setCancelable(false);



        cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int n, int n1, int n2) { // 달력을 클릭하면 해당 날짜의 db 데이터를 받아옴

                Pro.show(); // 오류방지를 위해 일부로 로딩화면을 띄움

                selectMonth = n1 + 1;

                if( (selectMonth) < 10) ms = "0" + selectMonth; // 01로 보내기위함
                else ms = "" + selectMonth;

                if(n2 < 10) ds = "0" + n2;
                else ds = "" + n2;

                s = "3" + n + "" + ms + "" + ds; // 320201112 로 보냄

                new Thread(){
                    public void run(){
                        try {
                            if(rock == 0) {
                                socket = new Socket(Ip, Port);
                                dos = new DataOutputStream(socket.getOutputStream());
                                dis = new DataInputStream(socket.getInputStream());
                                rock = 1;
                            }

                            Thread.sleep(100);
                            dos.writeUTF(s);
                            Thread.sleep(100);


                            for(int i = 0; i < 4; i++) {  // 여기부터
                                byte[] recv_data = new byte[10];
                                dis.read(recv_data);
                                String byte_msg = "";

                                for (byte b : recv_data) {
                                    char c = (char) b;
                                    byte_msg = byte_msg + c;
                                }


                                String[] msg_list = byte_msg.split(",");

                                n4 = Integer.valueOf(msg_list[0]);
                                n5 = Float.valueOf(msg_list[1]);
                                n6 = Integer.valueOf(msg_list[2]);

                                msg = new Msg(n4, n5, n6);

                                myTextView(); // 여기까지 데이터 받아오고 화면에 띄우기
                            }
                            Thread.sleep(100);
                            txtRock = 1;
                            Pro.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }.start();


            }
        });


    }

    public void myTextView() { // 화면 띄우기 객체


        new Thread() {
            public void run() {

                StateMsg state = new StateMsg(n5, n6);
                final String result = state.result();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(txtRock == 1){
                            txt1.setText("");
                            txt2.setText("");
                            txt3.setText("");
                            txt4.setText("");

                            txtRock = 0;
                        }

                        switch (msg.getDevice()) {
                            case 1:
                                txt1.setText("" + msg.getCelsius() + "℃/" + msg.getHumidity() + "%/" + result);
                                break;
                            case 2:
                                txt2.setText("" + msg.getCelsius() + "℃/" + msg.getHumidity() + "%/" + result);
                                break;
                            case 3:
                                txt3.setText("" + msg.getCelsius() + "℃/" + msg.getHumidity() + "%/" + result);
                                break;
                            case 4:
                                txt4.setText("" + msg.getCelsius() + "℃/" + msg.getHumidity() + "%/" + result);
                                break;
                            default:

                        }
                    }
                });
                }
        }.start();
    }



    protected void onStop(){
        super.onStop();

        try {
            new Thread(){
                public void run(){
                    try {
                        dos.writeUTF("2");
                        Thread.sleep(100);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            if(dos != null)dos.close();
            if(dis != null)dis.close();
            if(socket != null)socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }



}
