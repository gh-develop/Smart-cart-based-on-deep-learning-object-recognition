# -*- coding: utf-8 -*-
from ctypes import *
import random
import os
import cv2
import time
import darknet
import argparse
import pymysql
import socket
from threading import Thread, enumerate
from multiprocessing import Process, Queue
import numpy as np
# Reset SQL and Connect

# connect database
knormal = pymysql.connect(
    user='knormal',
    passwd='knormal@0102',
    host='3.37.3.112',
    db='knormal',
    charset='utf8'
)

weight_change_signal = 0  # 무게 변화 신호 저장하는 큐
weight_update_signal = 0  # 무게 업데이트 신호 저장하는 큐
add_number = 0
minus_number = 0
stable_signal = 0

def recvall(sock,count):
    buf = b''
    while count:
        newbuf = sock.recv(count)
        if not newbuf: return None
        buf+=newbuf
        count -= len(newbuf)
    return buf

def Send(conn, one):
    while True:
        send_data = bytes(one.encode()) # 사용자 입력
        conn.send(send_data) # Client -> Server 데이터 송신



# arguments setting
def parser():
    parser = argparse.ArgumentParser(description="YOLO Object Detection")
    parser.add_argument("--input", type=str, default="/home/ubuntu/han/test_video.mp4",
                        help="video source. If empty, uses video file")
    parser.add_argument("--out_filename", type=str, default="",
                        help="inference video name. Not saved if empty")
    parser.add_argument("--weights", default="./cfg/yolov4-tiny-custom_10th_best.weights",
                        help="yolo weights path")
    parser.add_argument("--dont_show", action='store_true',
                        help="windown inference display. For headless systems")
    parser.add_argument("--ext_output", action='store_true',
                        help="display bbox coordinates of detected objects")
    parser.add_argument("--config_file", default="./cfg/yolov4-tiny-custom_10th.cfg",
                        help="path to config file")
    parser.add_argument("--data_file", default="./cfg/obj.data",
                        help="path to data file")
    parser.add_argument("--thresh", type=float, default=.75,
                        help="remove detections with confidence below this value")
    return parser.parse_args()


def str2int(video_path):
    """
    argparse returns and string althout webcam uses int (0, 1 ...)
    Cast to int if needed
    """
    try:
        return int(video_path)
    except ValueError:
        return video_path


def check_arguments_errors(args):
    assert 0 < args.thresh < 1, "Threshold should be a float between zero and one (non-inclusive)"
    if not os.path.exists(args.config_file):
        raise (ValueError("Invalid config path {}".format(os.path.abspath(args.config_file))))
    if not os.path.exists(args.weights):
        raise (ValueError("Invalid weight path {}".format(os.path.abspath(args.weights))))
    if not os.path.exists(args.data_file):
        raise (ValueError("Invalid data file path {}".format(os.path.abspath(args.data_file))))
    if str2int(args.input) == str and not os.path.exists(args.input):
        raise (ValueError("Invalid video path {}".format(os.path.abspath(args.input))))


def set_saved_video(input_video, output_video, size):
    fourcc = cv2.VideoWriter_fourcc(*"MJPG")
    fps = int(input_video.get(cv2.CAP_PROP_FPS))
    video = cv2.VideoWriter(output_video, fourcc, fps, size)
    return video


def convert2relative(bbox, darknet_height, darknet_width):
    """
    YOLO format use relative coordinates for annotation
    """
    x, y, w, h = bbox
    _height = darknet_height
    _width = darknet_width
    return x / _width, y / _height, w / _width, h / _height


def convert2original(image, bbox, darknet_height, darknet_width):
    x, y, w, h = convert2relative(bbox, darknet_height, darknet_width)

    image_h, image_w, __ = image.shape

    orig_x = int(x * image_w)
    orig_y = int(y * image_h)
    orig_width = int(w * image_w)
    orig_height = int(h * image_h)

    bbox_converted = (orig_x, orig_y, orig_width, orig_height)

    return bbox_converted


def convert4cropping(image, bbox):
    x, y, w, h = convert2relative(bbox)

    image_h, image_w, __ = image.shape

    orig_left = int((x - w / 2.) * image_w)
    orig_right = int((x + w / 2.) * image_w)
    orig_top = int((y - h / 2.) * image_h)
    orig_bottom = int((y + h / 2.) * image_h)

    if (orig_left < 0): orig_left = 0
    if (orig_right > image_w - 1): orig_right = image_w - 1
    if (orig_top < 0): orig_top = 0
    if (orig_bottom > image_h - 1): orig_bottom = image_h - 1

    bbox_cropping = (orig_left, orig_top, orig_right, orig_bottom)

    return bbox_cropping


def video_capture(frame_queue,refr_queue, darknet_image_queue, cap, darknet_width, darknet_height):
    while cap:
        frame = refr_queue.get()
        #if not ret:
        #    break
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        frame_resized = cv2.resize(frame_rgb, (darknet_width, darknet_height),
                                   interpolation=cv2.INTER_LINEAR)
        frame_queue.put(frame)
        img_for_detect = darknet.make_image(darknet_width, darknet_height, 3)
        darknet.copy_image_from_bytes(img_for_detect, frame_resized.tobytes())
        darknet_image_queue.put(img_for_detect)
    #cap.release()


def inference(darknet_image_queue, detections_queue, fps_queue, cap, network, class_names, args, md_id, cursor, user_id,conn):
    # all detection labels are stored in check_detection variable
    check_detection = []
    global weight_change_signal
    global weight_update_signal
    global stable_signal
    # 무게변화가 있으면: 1, 아니면 0
    # weight_change_signal = 1

###
   # weight_queue2 = weight_queue
###
    while cap:
        #print("wei pip {}".format(weight_queue))
        proc = os.getpid()
        darknet_image = darknet_image_queue.get()
        prev_time = time.time()
        detections = darknet.detect_image(network, class_names, darknet_image, thresh=args.thresh)
        detections_queue.put(detections)
        fps = int(1 / (time.time() - prev_time))
        fps_queue.put(fps)
        print("FPS by process id {}: {}".format(proc, fps))
        label = darknet.print_detections(detections, args.ext_output)

        # update database using detection label
        if label is not None:
            check_detection.append(label)
            print("inference function: object detection occur = {}".format(label))
            print("inference function: weight_change_signal = {}".format(weight_change_signal))
        if weight_change_signal == 1:
            if weight_update_signal == -1 and stable_signal < 3:
                stable_signal += 1
            if stable_signal == 1:
                check_detection.clear()
            print(check_detection)
            send_query(check_detection, weight_update_signal, cursor, md_id, user_id,conn)

            
        darknet.free_image(darknet_image)
    #cap.release()


def drawing(frame_queue, detections_queue, fps_queue, cap, args, class_colors, darknet_height, darknet_width):
    random.seed(3)  # deterministic bbox colors
    #video = set_saved_video(cap, args.out_filename, (video_width, video_height))
    while cap:
        # frame_queue는 main에 있는 변수이고 여기에는 video_capture함수에서 입력한 영상정보가 저장돼있음
        frame = frame_queue.get()
        detections = detections_queue.get()
        fps = fps_queue.get()
        detections_adjusted = []
        if frame is not None:
            for label, confidence, bbox in detections:
                bbox_adjusted = convert2original(frame, bbox, darknet_height, darknet_width)
                detections_adjusted.append((str(label), confidence, bbox_adjusted))
            image = darknet.draw_boxes(detections_adjusted, frame, class_colors)
            if not args.dont_show:
                cv2.imshow('Inference', image)
           # if args.out_filename is not None:
           #     video.write(image)
            if cv2.waitKey(fps) == 27:
                break
    #cap.release()
    #video.release()
    cv2.destroyAllWindows()






def receive_vw(refr_queue, conn, cap):
    global weight_change_signal
    global weight_update_signal
    past_weight = 0
    jump = -1
    while cap:
            length =(recvall(conn,16)).decode(errors='ignore')
       
        #length = int(length)
       # print("len:: {}".format(length))
       # print("type::{}".format(type(length)))

            #print("len:: {}".format(length))
            Data = recvall(conn, int(length))
        # 영상데이터일 때
            if len(Data)> 10:
                video_data = np.fromstring(Data , dtype='uint8')
                video_data = cv2.imdecode(video_data,1)
                refr_queue.put(video_data)
        # 무게데이터일 때
            else:
                weight_data = int(Data)
                print("receive_vw function: weight:{}".format(weight_data))
                current_weight = weight_data
                weight_difference = abs(current_weight - past_weight)

                print("receive_vw function: actual difference weight is {}".format(current_weight-past_weight))
                if weight_difference >= 30 and jump == -1:
                    print("receive_vw function: weight_difference occur is {}".format(current_weight-past_weight))
                    jump = 0  # 무게 안정화 변수
                    stable_past_weight = past_weight  # 무게 안정화 알고리즘에서 사용할 과거 무게값
                if jump != -1 and jump < 3:
                    jump += 1

                    if jump == 3:
                        weight_change_signal = 1
                        jump = -1
                        print("receive_vw function: stable weight differenc is {}".format(current_weight - stable_past_weight))
                        if (current_weight - stable_past_weight) > 0:
                            weight_update_signal = 1

                        else:
                            weight_update_signal = -1

                    # weight_change_signal = 1
                    #
                    # if (current_weight - past_weight) > 0:
                    #     weight_update_signal = 1
                    #
                    # else:
                    #     weight_update_signal = -1

                past_weight = current_weight
        
       
            # weight_queue.put(weight_data)
            # print("weight:{}".format(weight_data))
       # if cv2.waitKey(1) == ord('q'):
       #     break


def model_start(conn, model_id):
    from queue import Queue
    frame_queue = Queue()
    refr_queue = Queue()
    darknet_image_queue = Queue(maxsize=1)
    detections_queue = Queue(maxsize=1)
    fps_queue = Queue(maxsize=1)
    # weight_queue = Queue()  # 무게값을 저장하는 큐
    # weight_change_signal = 0  # 무게 변화 신호 저장하는 큐
    # weight_update_signal = 0  # 무게 업데이트 신호 저장하는 큐

    # QR코드 인식
    length0 = (recvall(conn, 16)).decode()
    data0 = (recvall(conn, int(length0))).decode()
    print("length0 {}".format(length0))
    print("data0 {}".format(data0))
    if len(data0) > 0:
        id_data = str(data0)
        print("{} connected!!".format(id_data))

    md_id = model_id
    cursor = knormal.cursor(pymysql.cursors.DictCursor)
    # initialize USERBASKET TABLE
    sql = "UPDATE USERBASKET{} SET classNum = '0'".format(model_id)
    cursor.execute(sql)
    sql = "SELECT * FROM `USERBASKET{}`;".format((model_id))
    cursor.execute(sql)
    knormal.commit()

    # check initial value in USERBASKET TABLE
    result = cursor.fetchall()
    result = list(result)
    print("this is {} table: {}".format(model_id, result))

    user_id = id_data
    # ORDER 테이블 생성
    create_order_table_sql = "insert into USERORDER(userID) values('{}');".format(user_id)
    cursor.execute(create_order_table_sql)
    order_table_check_sql = "SELECT * FROM `USERORDER`;"
    cursor.execute(order_table_check_sql)
    knormal.commit()

    result = cursor.fetchall()
    result = list(result)
    print("USERORDER TABLE IS : {}".format(result))

    args = parser()
    check_arguments_errors(args)
    network, class_names, class_colors = darknet.load_network(
        args.config_file,
        args.data_file,
        args.weights,
        batch_size=1
    )
    darknet_width = darknet.network_width(network)
    darknet_height = darknet.network_height(network)
    #input_path = str2int(input_source)
    #cap = cv2.VideoCapture(input_path)
    video_width = int(640)
    video_height = int(480)
    cap = 1
    th5 = Thread(target=receive_vw, args=(refr_queue, conn, cap))
    th2 = Thread(target=video_capture, args=(frame_queue,refr_queue, darknet_image_queue, cap, darknet_width, darknet_height))
    th3 = Thread(target=inference, args=(darknet_image_queue, detections_queue, fps_queue, cap, network, class_names, args, md_id, cursor, user_id, conn))
    th4 = Thread(target=drawing, args=(frame_queue, detections_queue, fps_queue,  cap, args, class_colors, darknet_height, darknet_width))

    #daemon스레드 생성: 메인스레드가 종료되면 같이 종료
    th2.daemon = True
    th3.daemon = True
    th4.daemon = True
    th5.daemon = True

    th2.start()
    th3.start()
    th4.start()
    th5.start()
    th2.join()
    th3.join()
    th4.join()
    th5.join()

    print("model end")


def send_query(check_detection, weight_update_value, cursor, md_id, user_id,conn):
    global weight_update_signal
    global weight_change_signal
    global add_number
    if len(check_detection) !=0:
        max_value = max(set(check_detection), key=check_detection.count)
        find_order_id_sql = "SELECT order_id from USERORDER where userID = '{}';".format(user_id)
        cursor.execute(find_order_id_sql)
        row = cursor.fetchall()

    # order_id 저장
        order_id = row[-1]["order_id"]
        print(type(order_id))
        print("send query function: detect order_id in send_query: {}".format(order_id))
        print("send query function: weight_update_signal:{}".format(weight_update_signal))
    # Product price 저장
        find_price_sql = "SELECT Price from SHOPBASKET where e_name = '{}';".format(max_value)
        cursor.execute(find_price_sql)
        row = cursor.fetchall()
        price = row[0]["Price"]
        print("send query function: detect price in send _query: {}".format(price))
        if weight_update_value == 1 and len(check_detection) >= 3:
            increase_sql = "insert into USERBASKET(userID, productName, productPrice, classNum, order_id) VALUES ('{}', '{}', {}, 1, '{}') ON DUPLICATE KEY UPDATE classNum=classNum+1;".format(user_id, max_value, price, order_id)
        # "UPDATE USERBASKET{} SET classNum = classNum+1 WHERE productName = '{}';".format(md_id, max_value)
            cursor.execute(increase_sql)
            knormal.commit()
            print("send query function: userID is {}, increase +++++++++++++++++++++++++++++++++ {}".format(user_id, max_value))
            add_number += 1
            print("send query function: add_number is {}".format(add_number))
            weight_update_signal = 0
            weight_change_signal = 0
            one = str(1)
            Send(conn, one)
        # check_detection 초기화
            check_detection.clear()
        elif weight_update_value == -1 and len(check_detection) >= 3:
            decrease_sql = "UPDATE USERBASKET SET classNum = classNum-1 where productName = '{}' and classNum >0 and userID = '{}' and order_id = '{}';".format(max_value, user_id, order_id)
            cursor.execute(decrease_sql)
            knormal.commit()
            print("send query function: userID is {}, decrease ---------------------------------------------{}".format(user_id, max_value))
            minus_number += 1
            print("send query function: minus_number is {}".format(minus_number))
            weight_update_signal = 0
            weight_change_signal = 0
            stable_signal = 0

            one = str(1)
            Send(conn, one)
        # check_detection 초기화
            check_detection.clear()


if __name__ == '__main__':
    ip = '172.31.5.252'  # ip 주소
    port = 5902  # port 번호

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # 서버소켓 객체를 생성
    s.bind((ip, port))  # 바인드(bind) : 소켓에 주소, 프로토콜, 포트를 할당
    s.listen(10)  # 연결 수신 대기 상태(리스닝 수(동시 접속) 설정)
    print('클라이언트 연결 대기')
    count = 0
    while True:
        count += 1
        # 연결 수락(클라이언트 소켓 주소를 반환)
        conn, addr = s.accept()  # conn: 클라이언트 소켓
        print('Connected ' + str(addr)) # 클라이언트 주소 출력


        if count == 1:
            print('count value:{}'.format(count))
            th0 = Process(target=model_start, args=(conn, count))
            th0.start()
            # th0.join()
        else:
            print('count value:{}'.format(count))
            th1 = Process(target=model_start, args=(conn, count))
            th1.start()
            # th1.join()

    # model_start
    # model_id는 user_id로 변경
    # input source는 conn객체로 변경
    # model_id = 1
    # th0 = Process(target=model_start, args=("/home/ubuntu/han/2.mp4", model_id))
    # model_id += 1
    # th1 = Process(target=model_start, args=("/home/ubuntu/han/1.mp4", model_id))
    # th0.start()
    # th1.start()
    # th0.join()
    # th1.join()





