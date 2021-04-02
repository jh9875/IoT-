#include <DHT.h>
#include <DHT_U.h>
#define LED_PIN 10           //LED가 연결된 디지털 핀
#define DEVICE_ONE 2000000  // 모트별 번호 설정
#define DHTPIN 6    //온습도 센서가 연결된 디지털핀

#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321

DHT dht(DHTPIN, DHTTYPE);
#include <SoftwareSerial.h> // 0,1번핀 제외하고 Serial 통신을 하기 위해 선언


// SoftwareSerial 통신핀으로 D2번핀을 Tx로, D3번핀을 Dx로 선언

SoftwareSerial mySerial(2, 3);


void setup() {
  pinMode (8, OUTPUT);
  digitalWrite(8, HIGH);

  Serial.begin(9600);
  mySerial.begin(38400); // 통신 속도 9600bps로 블루투스 시리얼 통신 시작

  Serial.println("START!");
  pinMode( LED_PIN, OUTPUT );

  dht.begin();
}

void loop() {


  //측정하는 시간사이에 1초간의 딜레이를 줌
  delay(1000);

  long  hum = dht.readHumidity(); //습도값의 정수 부분을 취한다.

  float cel = dht.readTemperature();//온도값을 읽어옴
  //에러 검사
  if (isnan(hum) || isnan(cel) ) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  long code = 0;
  long bel = 0;
  long tranCel = 0;
  static int sign = 0;
  if (mySerial.available()) {  // 값이 들어온다면.

    sign = mySerial.parseInt();
    if (sign == 1) //  요청 1, 비요청 2
    {
        bel = 0;             // 영상 영하값 ( 1일경우 영하, 0일경우 영상)
        if (cel < 0)
        {
          bel = bel + 100000;
          cel = -cel;
        }
        tranCel = (long)(cel * 1000) ; // 변환된 온도 값, 소수점 한자리 까지 
        code = DEVICE_ONE + bel + tranCel + hum ;  //0(디바이스번호) 0(영하 또는 영상) 000(온도값)00(습도값) -> 0000000 (7자리)

        //LED 부분
        digitalWrite(LED_PIN, HIGH);
        delay(500);
        digitalWrite(LED_PIN, LOW);
        delay(500);

        Serial.println(code);
        mySerial.print(code);
        Serial.println("전송완료");
        delay(700);
        sign = mySerial.parseInt();
    }

  }
}
