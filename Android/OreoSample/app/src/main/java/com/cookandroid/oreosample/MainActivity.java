package com.cookandroid.oreosample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {

    Button btn1, btn2, btn3;
    TextView txt1,txt2,txt3,txt4,txt5,txt6,txt7,txt8,txt9,txt10,txt11,txt12;
    EditText conEtxt1, conEtxt2;
    LinearLayout lay1,lay2,lay3,lay4;
    View connectView;
    String Ip;
    int Port;
    public static Context context;
    String s = null;
    Connect conn = null;
    String onOff;
    float celsius;
    int humidity;
    String str1,str2;
    Msg msg = null;
    StateMsg state = null;
    SharedPreferences sp = null;
    SharedPreferences.Editor editor = null;
    Intent intent;
    int rock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);

        txt1 = (TextView) findViewById(R.id.txt1);
        txt2 = (TextView) findViewById(R.id.txt2);
        txt3 = (TextView) findViewById(R.id.txt3);
        txt4 = (TextView) findViewById(R.id.txt4);
        txt5 = (TextView) findViewById(R.id.txt5);
        txt6 = (TextView) findViewById(R.id.txt6);
        txt7 = (TextView) findViewById(R.id.txt7);
        txt8 = (TextView) findViewById(R.id.txt8);
        txt9 = (TextView) findViewById(R.id.txt9);
        txt10 = (TextView) findViewById(R.id.txt10);
        txt11 = (TextView) findViewById(R.id.txt11);
        txt12 = (TextView) findViewById(R.id.txt12);

        lay1 = (LinearLayout) findViewById(R.id.lay1);
        lay2 = (LinearLayout) findViewById(R.id.lay2);
        lay3 = (LinearLayout) findViewById(R.id.lay3);
        lay4 = (LinearLayout) findViewById(R.id.lay4);

        sp = getSharedPreferences("ipFile",MODE_PRIVATE);
        editor = sp.edit();
        intent = new Intent(MainActivity.this,SocketService.class);
        rock = 0;


        context = this;

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    // 연결버튼

                onOff = "2";
                stopService(intent);

                connectView = (View) View.inflate(MainActivity.this,R.layout.connect,null);    // ip,port입력창 띄우기
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setView(connectView);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        conEtxt1 = (EditText) connectView.findViewById(R.id.etxt1);
                        conEtxt2 = (EditText) connectView.findViewById(R.id.etxt2);

                        Ip = conEtxt1.getText().toString();
                        Port = Integer.parseInt(conEtxt2.getText().toString());

                        editor.putString("IP",Ip);    // ip,port 저장 , 자동 입력되도록
                        editor.putInt("PORT",Port);
                        editor.commit();

                        conn = new Connect(Ip,Port);

                        rock = 0;

                        btn2.callOnClick();


                    }
                });
                dlg.setNegativeButton("취소",null);
                dlg.show();


            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {  // 시작버튼

                if(rock == 0){
                    rock = 1;      // 소켓이 중복 실행 안되기 위한 락
                    onOff = "1";    // 시작 중지 값을 전달하기 위한 변수
                    try {
                        Thread.sleep(500);    // 중지를 눌렀다 너무 빠르게 시작을 눌러서 경합조건이 일어나는걸 방지하기 위함
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startService(intent);}    // 소켓 시작부분


            }
        });



        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    //중지버튼

                rock = 0;
                onOff = "2";

                try {
                    Thread.sleep(1000);  // 데이터가 다 들어온 후 초기화하기 위함
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {  // 화면 초기화
                        txt1.setText("");
                        txt2.setText("");
                        txt3.setText("");
                        txt4.setText("");
                        txt5.setText("");
                        txt6.setText("");
                        txt7.setText("");
                        txt8.setText("");
                        txt9.setText("");
                        txt10.setText("");
                        txt11.setText("");
                        txt12.setText("");
                        lay1.setBackgroundColor(getResources().getColor(R.color.Good));
                        lay2.setBackgroundColor(getResources().getColor(R.color.Good));
                        lay3.setBackgroundColor(getResources().getColor(R.color.Good));
                        lay4.setBackgroundColor(getResources().getColor(R.color.Good));

                    }
                });

            }
        });

    }


    public void myTextView(){  // 데이터 화면에 띄우기

        msg = ((SocketService)SocketService.context2).msg2;
        celsius = msg.getCelsius();
        humidity = msg.getHumidity();

        str1 = Float.toString(celsius);
        str2 = Integer.toString(humidity);

        state = new StateMsg(celsius,humidity);
        s = state.result();
        if(onOff.equals("1")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    switch (msg.getDevice()){
                        case 1 :
                            txt1.setText("온도 : " + str1 + "℃");
                            txt2.setText("습도 : " + str2 + "%");
                            txt3.setText("" + s);
                            if(s.equals("좋음")) {lay1.setBackgroundColor(getResources().getColor(R.color.Good)); txt3.setTextColor(Color.WHITE);}
                            if(s.equals("양호")) {lay1.setBackgroundColor(getResources().getColor(R.color.Well)); txt3.setTextColor(Color.WHITE);}
                            if(s.equals("보통")) {lay1.setBackgroundColor(getResources().getColor(R.color.SoSo)); txt3.setTextColor(Color.WHITE);}
                            if(s.equals("나쁨")) {lay1.setBackgroundColor(getResources().getColor(R.color.Wanning)); txt3.setTextColor(Color.RED);}
                            break;

                        case 2:
                            txt4.setText("온도 : " + str1 + "℃");
                            txt5.setText("습도 : " + str2 + "%");
                            txt6.setText("" + s);
                            if(s.equals("좋음")) {lay2.setBackgroundColor(getResources().getColor(R.color.Good)); txt6.setTextColor(Color.WHITE);}
                            if(s.equals("양호")) {lay2.setBackgroundColor(getResources().getColor(R.color.Well)); txt6.setTextColor(Color.WHITE);}
                            if(s.equals("보통")) {lay2.setBackgroundColor(getResources().getColor(R.color.SoSo)); txt6.setTextColor(Color.WHITE);}
                            if(s.equals("나쁨")) {lay2.setBackgroundColor(getResources().getColor(R.color.Wanning)); txt6.setTextColor(Color.RED);}
                            break;

                        case 3:
                            txt7.setText("온도 : " + str1 + "℃");
                            txt8.setText("습도 : " + str2 + "%");
                            txt9.setText("" + s);
                            if(s.equals("좋음")) {lay3.setBackgroundColor(getResources().getColor(R.color.Good)); txt9.setTextColor(Color.WHITE);}
                            if(s.equals("양호")) {lay3.setBackgroundColor(getResources().getColor(R.color.Well)); txt9.setTextColor(Color.WHITE);}
                            if(s.equals("보통")) {lay3.setBackgroundColor(getResources().getColor(R.color.SoSo)); txt9.setTextColor(Color.WHITE);}
                            if(s.equals("나쁨")) {lay3.setBackgroundColor(getResources().getColor(R.color.Wanning)); txt9.setTextColor(Color.RED);}
                            break;

                        case 4:
                            txt10.setText("온도 : " + str1 + "℃");
                            txt11.setText("습도 : " + str2 + "%");
                            txt12.setText("" + s);
                            if(s.equals("좋음")) {lay4.setBackgroundColor(getResources().getColor(R.color.Good)); txt12.setTextColor(Color.WHITE);}
                            if(s.equals("양호")) {lay4.setBackgroundColor(getResources().getColor(R.color.Well)); txt12.setTextColor(Color.WHITE);}
                            if(s.equals("보통")) {lay4.setBackgroundColor(getResources().getColor(R.color.SoSo)); txt12.setTextColor(Color.WHITE);}
                            if(s.equals("나쁨")) {lay4.setBackgroundColor(getResources().getColor(R.color.Wanning)); txt12.setTextColor(Color.RED);}
                            break;

                        default :

                    }
                }
            }); }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0,1,0,"일자별 조회");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //메뉴 선택하면 db화면 시작
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case 1:

                btn3.callOnClick();

                intent = new Intent(getApplicationContext(), com.cookandroid.oreosample.Calendar.class);
                startActivity(intent);


                return true;
        }

        return false;
    }

    protected void onStart() {  // 모바일 실행시 입력기록이 있으면 자동연결


        Ip = sp.getString("IP","");
        if(Ip.equals("")){
            btn1.callOnClick();}
        else{
            Port = sp.getInt("PORT",0);
            conn = new Connect(Ip,Port);

            if(rock == 0){
                rock = 1;
                onOff = "1";
                startService(intent);}

        }



        super.onStart();
    }

    protected void onDestroy(){  // 어플을 중지시키면 정지

        btn3.callOnClick();

        super.onDestroy();
    }



}

