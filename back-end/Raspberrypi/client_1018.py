# -*- coding: utf-8 -*-
import cv2
import socket
import struct
import pickle
import numpy
import sys
import time
import pymysql
import threading
from rpi_ws281x import *
import argparse
import os
import signal

lock = threading.Lock()
cap = 1
# LED strip configuration:
LED_COUNT = 16  # Number of LED pixels.
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_FREQ_HZ = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA = 10  # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255  # Set to 0 for darkest and 255 for brightest
LED_INVERT = False  # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53


def cleanAndExit():
    print("Cleaning...")

    if not EMULATE_HX711:
        GPIO.cleanup()

    print("Bye!")
    pid = os.getpid()
    os.kill(pid, signal.SIGTERM)
    print('exit')
    sys.exit()


def weight(hx):
    global cap
    while True:
        # weight sensor
        weight_val = hx.get_weight(5)
        val = int(weight_val)
        print(val)

        # 무게값이 -10 이상이면
        if val > -10:
            hx.power_down()
            hx.power_up()

            valstr = str(val)
            lenval = str(len(valstr))

            lock.acquire()
            client_socket.send(lenval.ljust(16).encode())
            print('weight lenth')
            client_socket.send(valstr.encode())
            print('weight')
            lock.release()

        if cap == 0:
            print('client socket closed!')
            client_socket.close()
            cleanAndExit()


def video(camera, encode_param):
    global cap
    while cap:
        prev_time = time.time()
        ret, frame = camera.read()
        frame = cv2.flip(frame, 1)
        result, imgencode = cv2.imencode('.jpg', frame, encode_param)
        data = numpy.array(imgencode)

        stringData = data.tobytes()

        fps = int(1 / (time.time() - prev_time))

        cv2.imshow('CLIENT', frame)

        stringDatastr = stringData
        lenstringData = str(len(stringDatastr))
        lock.acquire()
        client_socket.send(lenstringData.ljust(16).encode())
        print('img lenth')
        client_socket.send(stringDatastr)
        print('img')
        lock.release()

        print("fps:{}".format(fps))
        if cv2.waitKey(1) & 0xff == ord('q'):
            cap = 0
            break
    camera.release()
    cv2.destroyAllWindows()


# Define functions which animate LEDs in various ways.
def colorWipe(strip, color, wait_ms=50):
    """Wipe color across display a pixel at a time."""
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color)
        strip.show()
        time.sleep(wait_ms / 1000.0)


def led(color):
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--clear', action='store_true', help='clear the display on exit')
    args = parser.parse_args()
    # Create NeoPixel object with appropriate configuration.
    strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
    # Intialize the library (must be called once before other functions).
    strip.begin()

    print('Press Ctrl-C to quit.')
    if not args.clear:
        print('Use "-c" argument to clear LEDs on exit')
    try:
        # while True:
        print('Color wipe animations.')

        if color == 1:  # red
            colorWipe(strip, Color(255, 0, 0))  # red wipe
            time.sleep(2)
            colorWipe(strip, Color(0, 0, 0))


        elif color == 2:  # green
            colorWipe(strip, Color(0, 255, 0))  # green wipe
            time.sleep(2)
            colorWipe(strip, Color(0, 0, 0))


        elif color == 3:  # blue
            colorWipe(strip, Color(0, 0, 255))  # Blue wipe
            time.sleep(2)
            colorWipe(strip, Color(0, 0, 0))


    except KeyboardInterrupt:
        if args.clear:
            colorWipe(strip, Color(0, 0, 0), 10)


def recv():
    while True:
        recv_data = client_socket.recv(1024).decode()  # Server -> Client 데이터 수신
        print("recieve data: {}".format(recv_data))
        if int(recv_data) == 1:
            led(1)
        elif int(recv_data) == 3:
            led(3)


if __name__ == '__main__':

    print("Input ID:")
    user_id = input()
    # print("Input PW:")
    # pws = input()
    idc = pwc = 0

    knormal = pymysql.connect(
        user='knormal',
        passwd='knormal@0102',
        host='3.37.3.112',
        db='knormal',
        charset='utf8'
    )

    cursor = knormal.cursor()
    query = "SELECT * FROM USER"
    cursor.execute(query)
    knormal.commit()
    datas = cursor.fetchall()
    print('order table updated')

    for data in datas:
        print(data)
        print(type(data))

        if user_id == data[0]:
            idc = 1
        # if pws == data[i]:
        #    pwc = 1

    # if pwc==idc==1:
    if idc == 1:
        print("Correct ID")

        ip = '3.37.3.112'  #
        port = 5902  #
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((ip, port))
        print('connection')

        # ORDER 테이블 생성
        create_order_table_sql = "insert into USERORDER(userID) values('{}');".format(user_id)
        cursor.execute(create_order_table_sql)
        order_table_check_sql = "SELECT * FROM `USERORDER`;"
        cursor.execute(order_table_check_sql)
        knormal.commit()

        # led
        # led(2)  # qr code success -> green light on

        # ID send
        idsstr = str(user_id)
        lenids = str(len(idsstr))
        client_socket.send((lenids.encode()).ljust(16));
        client_socket.send(idsstr.encode())

        # Camera
        camera = cv2.VideoCapture(-1, cv2.CAP_V4L)
        camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640);  #
        camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480);  #
        encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 90]

        # Weight sensor
        EMULATE_HX711 = False
        referenceUnit = 1

        if not EMULATE_HX711:
            import RPi.GPIO as GPIO
            from hx711 import HX711
        else:
            from emulated_hx711 import HX711

        hx = HX711(20, 16)
        hx.set_reading_format("MSB", "MSB")
        hx.set_reference_unit(261)
        # 261 1000
        hx.reset()
        hx.tare()
        print("Tare done! Add weight now...")

        # Thread

        t1 = threading.Thread(target=video, args=(camera, encode_param))
        t1.start()
        t2 = threading.Thread(target=weight, args=(hx,))
        t2.start()
        t3 = threading.Thread(target=recv, args=())
        t3.start()

        t1.join()
        t2.join()
        t3.join()



    else:
        print("Not Correct")
