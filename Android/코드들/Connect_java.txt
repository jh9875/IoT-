package com.cookandroid.oreosample;

public class Connect  {

    String IP;
    int PORT ;

    Connect(String ip, int port){
        this.IP = ip;
        this.PORT = port;
    }

    String getIP(){
        return IP;
    }

    int getPORT(){
        return PORT;
    }

}
