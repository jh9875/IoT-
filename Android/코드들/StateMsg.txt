package com.cookandroid.oreosample;

public class StateMsg {
    float celsius = -1; //온도
    int humidity = -1; // 습도
    String state = null;

    /*경고 - 습도 65퍼이상
      양호 - 습도 30~60 , 온도 26이하
      좋음 - 습도 40~60 , 온도 24이하
      보통 - 나머지 */

    StateMsg (float a, int b){
        this.celsius = a;
        this.humidity = b;

        if(humidity >= 65 ) state = "나쁨";
        else if((humidity >= 40 && humidity <=60) && celsius <= 24) state = "좋음";
        else if((humidity >= 30 && humidity <= 60) && celsius <= 26) state = "양호";
        else state = "보통";

    }

    String result(){
        return state;
    }

}
