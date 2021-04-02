package com.cookandroid.oreosample;

public class Msg {


    int device = 0;  // 1,2,3,4
    float celsius = 0; //온도
    int humidity = 0; // 습도


    public Msg(int a, float b, int c) {
        this.device = a;
        this.celsius = b;
        this.humidity = c;}


    public synchronized int getDevice(){
        return device;
    }

    public synchronized float getCelsius(){return celsius;}

    public synchronized int getHumidity(){
        return humidity;
    }




}
