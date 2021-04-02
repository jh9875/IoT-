import socket
from _thread import *
from bluetooth import *
import threading
import bluetooth
import time
import datetime
import pymysql as db

import Adafruit_DHT as DHT
import RPi.GPIO as GPIO



DHT_PIN =22
LED_PIN =16
GPIO.setmode(GPIO.BCM)
GPIO.setup(LED_PIN, GPIO.OUT, initial=GPIO.LOW)

def date_create():  # 현재 날짜를 생성
    yr = datetime.datetime.now().year
    mnt = datetime.datetime.now().strftime("%m")
    dy = datetime.datetime.now().strftime("%d")

    year, month, day = str(yr), str(mnt), str(dy)
    now_date = year+month+day

    return now_date

def send_date_data_application(client_socket, msg): # DB에 접속하여 일자별 조회 데이터를 보내는 부분
    conn = db.connect(host = "localhost", user = "root", passwd = "snslab", db = "snslab_2020", charset = "utf8")
    cursor = conn.cursor()
    device_set = ["1", "2", "3", "4"]

    for dev_id in device_set:
        sql = "select cels, high_hum from device" + dev_id + " where date_time = " + msg
        cursor.execute(sql)

        for row in cursor:
            that_cels = row[0]
            high_hum = row[1]

        if(float(that_cels) >= 10):
            cels = "+" + str(that_cels)
        elif(float(that_cels) >= 0 and float(that_cels) < 10):
            cels = "+0" + str(that_cels)
        elif(float(that_cels) < 0 and float(that_cels) > -10):
            cels = "-0" + str(that_cels)[1:]
        else:
            cels = str(that_cels)

        if(int(high_hum) >= 0 and int(high_hum) < 10):
            hum = "0" + str(high_hum)
        else:
            hum = str(high_hum)

        date_str = dev_id + "," + cels + "," + hum

        client_socket.send(date_str.encode())

        # update period
        time.sleep(0.5)

    cursor.close()
    conn.close()

# 라즈베리파이 센서값 받는 부분
def getData():
    hum, temp =DHT.read_retry(DHT.DHT22, DHT_PIN)
    return hum, temp

# 아두이노에 명령 보내는 부분
def sendToArduino(socket, alert):
    socket.send(str(alert).encode())

# 아두이노로부터 데이터를 전송받아 메시지 구조체로 변경한 후
# 안드로이드로 보내는 부분
def Wifi_thread(wifi_client_socket, now_action, high_hum):

    conn = db.connect(host = "localhost", user = "root", passwd = "snslab", db = "snslab_2020", charset = "utf8")
    cursor = conn.cursor()
    is_start = 0

    try:
        while 1:
            now_date = date_create()
            #블루투스 소켓을 순차적으로 명령 실행

            for blue_socket in blue_socket_set:
                sock_msg = wifi_client_socket.recv(1024)
                sck_msg = sock_msg.decode()

                if(sck_msg[2] == "2"):
                    for blue_socket in blue_socket_set:
                        sendToArduino(blue_socket, 2)
                else:
                    if(is_start == 0):
                        print("start")
                        # 각 블루투스에서 수신받은 데이터를 어플리케이션으로 송신하는 루프
                        sendToArduino(blue_socket, now_action)
                        msg1 = blue_socket.recv(1024)
                        time.sleep(0.1) # 너무 빠르게 받으면 데이터가 중첩되는 현상을 방지하기 위한 sleep
                        msg2 = blue_socket.recv(1024)
                        sendToArduino(blue_socket, 2)

                    if(is_start > 0):

                        sendToArduino(blue_socket, now_action)
                        # error code
                        msg1 = blue_socket.recv(1024)
                        time.sleep(0.1)
                        msg2 = blue_socket.recv(1024)
                        sendToArduino(blue_socket, 2)

                    msg_dat1 = msg1.decode()
                    msg_dat2 = msg2.decode()

                    s_data = str(msg_dat1) + str(msg_dat2)
                    dev_id = s_data[0]  # device 번호
                    celsius = s_data[2:4]
                    celsius = celsius + "." + s_data[4]

                    if(s_data[1] == '1'):
                        celsius = "-" + celsius
                    elif(s_data[1] == '0'):
                        celsius = "+" + celsius

                    humidity = s_data[5:7]

                    print(s_data)

                    exist_sql = "select * from device" + dev_id + " where date_time = " + now_date
                    exist_date = cursor.execute(exist_sql) # sql문 실행후 empty set이면 0이 반환

                    # 해당 날짜의 기록이 존재하지 않으면 테이블에 해당 날짜를 생성
                    if(exist_date == 0):
                        insert_sql = "insert into device" + dev_id + " values("+ now_date +", 0, 0)"
                        cursor.execute(insert_sql)
                        conn.commit()

                    sel_sql = "select high_hum from device" + dev_id + " where date_time = " + now_date
                    cursor.execute(sel_sql)
                    for row in cursor:
                        high_hum = row[0]

                    if(int(humidity) > high_hum):
                        update_sql = "update device" + dev_id + " set cels = " + celsius + ", high_hum = " + humidity
                        cursor.execute(update_sql)
                        conn.commit()

                    data_set = dev_id + "," + celsius + "," + humidity
                    wifi_client_socket.send(data_set.encode())

                    is_start += 1

            # 라즈베리파이 데이터 전송 부분
            hum, temp =getData()
            if hum is not None and temp is not None:
                GPIO.output(LED_PIN, GPIO.HIGH)
            else:
                print("failed to get data")

            temp =round(temp, 1)

            if(temp>0):
                str_temp = "+" + str(temp)
            hum =int(hum)

            exist_sql = "select * from device4 where date_time = " + now_date
            exist_date = cursor.execute(exist_sql)

            if(exist_date == 0):
                insert_sql = "insert into device4 values("+ now_date +", 0, 0)"
                cursor.execute(insert_sql)
                conn.commit()

            sel_sql = "select high_hum from device4 where date_time = " + now_date
            cursor.execute(sel_sql)
            for row in cursor:
                high_hum = row[0]

            if(hum > high_hum):
                update_sql = "update device4 set cels = " + str_temp + ", high_hum = " + str(hum)
                cursor.execute(update_sql)
                conn.commit()

            data_set =str(4) + "," + str_temp + "," + str(hum)
            print(data_set)

            GPIO.output(LED_PIN, GPIO.LOW)
            wifi_client_socket.send(data_set.encode())

    except Exception as e:
        data_set = ""
        print("except : ",e)
        cursor.close()
        conn.close()
    # 클라이언트가 접속을 끊을 때 까지 반복합니다.

HOST = ''
PORT = 4123
addr = 0
port = 1

# 소켓 생성 = 안드로이드와 통신
wifi_server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
wifi_server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
wifi_server_socket.bind((HOST, PORT))
# 받아들일 블루투스 연결의 개수만큼 생성
# 루프를 돌며 연결을 시도하고, 연결이 실패하면 다시 루프를 돌게 함.
while True:
    blue_socket1 = BluetoothSocket( RFCOMM )
    try:
        blue_socket1.connect(("98:D3:71:F5:FF:FF", port))
        print("blue1 success")
        break
    except:
        pass

while True:
    blue_socket2 = BluetoothSocket( RFCOMM )
    try:
        blue_socket2.connect(("98:D3:31:F9:97:DB", port))
        print("blue2 success")
        break
    except:
        pass


while True:
    blue_socket3 = BluetoothSocket( RFCOMM )
    try:
        blue_socket3.connect(("98:D3:71:F6:00:33", port))
        print("blue3 success")
        break
    except:
        pass

blue_socket_set = [blue_socket1, blue_socket2, blue_socket3]

# 불안정하게 종료할 경우를 대비하여 전송루프를 돌지 않도록 우선 초기화
for blue_socket in blue_socket_set:
    sendToArduino(blue_socket, 2)

# 초기 실행부분
while True:
    print("Run")
    wifi_server_socket.listen()
    client_socket, addr = wifi_server_socket.accept()
    print(addr)

    high_hum = 0

    onOff = client_socket.recv(1024)
    msg = onOff.decode()
    now_action = int(msg[2])

    if(now_action == 1):
        Wifi_thread(client_socket, now_action, high_hum)

    elif(now_action == 3):
        date_msg = msg[3:]
        send_date_data_application(client_socket, date_msg)

    # 여기는 except시에 실행할 부분 (강제종료)
    if(now_action == 1):
        now_action = 2
        print("error!")

        for blue_socket in blue_socket_set:
            sendToArduino(blue_socket, now_action)
        addr = 0
