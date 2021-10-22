 # -*- coding: utf-8 -*-
from ctypes import *
import random
import os
from typing import Any
import cv2
import time
import darknet
import argparse
from threading import Thread, enumerate
from queue import Queue
import sys
##
import socket
import pickle
import struct
import numpy as np
##

def parser():
    parser = argparse.ArgumentParser(description="YOLO Object Detection")
    parser.add_argument("--input", type=str, default="/home/ubuntu/han/2.mp4",
                        help="video source. If empty, uses video file")
    parser.add_argument("--out_filename", type=str, default="",
                        help="inference video name. Not saved if empty")
    parser.add_argument("--weights", default="yolov4-tiny-custom_3th.weights",
                        help="yolo weights path")
    parser.add_argument("--dont_show", action='store_true',
                        help="windown inference display. For headless systems")
    parser.add_argument("--ext_output", action='store_true',
                        help="display bbox coordinates of detected objects")
    parser.add_argument("--config_file", default="./cfg/yolov4-tiny-custom_3th.cfg",
                        help="path to config file")
    parser.add_argument("--data_file", default="./cfg/obj.data",
                        help="path to data file")
    parser.add_argument("--thresh", type=float, default=.25,
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
        raise(ValueError("Invalid config path {}".format(os.path.abspath(args.config_file))))
    if not os.path.exists(args.weights):
        raise(ValueError("Invalid weight path {}".format(os.path.abspath(args.weights))))
    if not os.path.exists(args.data_file):
        raise(ValueError("Invalid data file path {}".format(os.path.abspath(args.data_file))))
    if str2int(args.input) == str and not os.path.exists(args.input):
        raise(ValueError("Invalid video path {}".format(os.path.abspath(args.input))))


def set_saved_video(input_video, output_video, size):
    fourcc = cv2.VideoWriter_fourcc(*"MJPG")
    fps = int(input_video.get(cv2.CAP_PROP_FPS))
    video = cv2.VideoWriter(output_video, fourcc, fps, size)
    return video


def convert2relative(bbox):
    """
    YOLO format use relative coordinates for annotation
    """
    x, y, w, h  = bbox
    _height     = darknet_height
    _width      = darknet_width
    return x/_width, y/_height, w/_width, h/_height


def convert2original(image, bbox):
    x, y, w, h = convert2relative(bbox)

    image_h, image_w, __ = image.shape

    orig_x       = int(x * image_w)
    orig_y       = int(y * image_h)
    orig_width   = int(w * image_w)
    orig_height  = int(h * image_h)

    bbox_converted = (orig_x, orig_y, orig_width, orig_height)

    return bbox_converted


def convert4cropping(image, bbox):
    x, y, w, h = convert2relative(bbox)

    image_h, image_w, __ = image.shape

    orig_left    = int((x - w / 2.) * image_w)
    orig_right   = int((x + w / 2.) * image_w)
    orig_top     = int((y - h / 2.) * image_h)
    orig_bottom  = int((y + h / 2.) * image_h)

    if (orig_left < 0): orig_left = 0
    if (orig_right > image_w - 1): orig_right = image_w - 1
    if (orig_top < 0): orig_top = 0
    if (orig_bottom > image_h - 1): orig_bottom = image_h - 1

    bbox_cropping = (orig_left, orig_top, orig_right, orig_bottom)

    return bbox_cropping


def video_capture(frame_queue, darknet_image_queue, data, cap):
   # while cap.isOpened():
#s
    while cap:
        # 프레임 수신
        while len(data) < payload_size:
            data += conn.recv(4096)
        packed_msg_size = data[:payload_size]
        data = data[payload_size:]
        msg_size = struct.unpack(">L", packed_msg_size)[0]
        while len(data) < msg_size:
            data += conn.recv(4096)
        frame_data = data[:msg_size]
        data = data[msg_size:]
        # print("Frame Size : {}".format(msg_size))  # 프레임 크기 출력

        # 역직렬화(de-serialization) : 직렬화된 파일이나 바이트를 원래의 객체로 복원하는 것
        frame = pickle.loads(frame_data, fix_imports=True, encoding="bytes")  # 직렬화되어 있는 binary file로 부터 객체로 역직렬화    
        frame = cv2.imdecode(frame, cv2.IMREAD_COLOR)  # 프레임 디코딩
#e
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        frame_resized = cv2.resize(frame_rgb, (darknet_width, darknet_height),
                                   interpolation=cv2.INTER_LINEAR)
        frame_queue.put(frame)
        img_for_detect = darknet.make_image(darknet_width, darknet_height, 3)
        darknet.copy_image_from_bytes(img_for_detect, frame_resized.tobytes())
        darknet_image_queue.put(img_for_detect)
    #cv2.destroyAllWindows()

def inference(darknet_image_queue, detections_queue, fps_queue,cap):
    while cap:
        darknet_image = darknet_image_queue.get()
        prev_time = time.time()
        detections = darknet.detect_image(network, class_names, darknet_image, thresh=args.thresh)
        detections_queue.put(detections)
        fps = int(1/(time.time() - prev_time))
        fps_queue.put(fps)
        print("FPS: {}".format(fps))
        
        #darknet.py에 들어있는 print_detections함수를 호출
        #라벨 정보를 받아오기 위해서 print)detections함수 수정 필요
        darknet.print_detections(detections, args.ext_output)
        darknet.free_image(darknet_image)
    #cv2.destroyAllWindows()


def drawing(frame_queue, detections_queue, fps_queue,cap):
    random.seed(3)  # deterministic bbox colors
    # video = set_saved_video(cap, args.out_filename, (video_width, video_height))
    while True:
        # frame_queue는 main에 있는 변수이고 여기에는 video_capture함수에서 입력한 영상정보가 저장돼있음
        frame = frame_queue.get()
        detections = detections_queue.get()
        fps = fps_queue.get()
        detections_adjusted = []
        if frame is not None:
            for label, confidence, bbox in detections:
                bbox_adjusted = convert2original(frame, bbox)
                detections_adjusted.append((str(label), confidence, bbox_adjusted))
            
            # darknet.py에 들어있는 draw_boxes함수를 호출
            # draw_boxes에도 라벨 정보가 들어있음, 수정 필요
            image, label = darknet.draw_boxes(detections_adjusted, frame, class_colors)
            if not args.dont_show:
                cv2.imshow('Inference', image)
                
                # label출력 확인
                cv2.putText(image, label, (30, 30), cv2.FONT_HERSHEY_PLAIN, 2, (255,0,0), 2)
            #if args.out_filename is not None:
               # video.write(image)
            if cv2.waitKey(30) & 0xFF ==ord('q'):
                cap=0
                break
   
    # video.release()
    cv2.destroyAllWindows()


if __name__ == '__main__':
##s
    ip = '172.31.5.252'  # ip 주소
    port = 5902  # port 번호

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # 소켓 객체를 생성
    s.bind((ip, port))  # 바인드(bind) : 소켓에 주소, 프로토콜, 포트를 할당
    s.listen(10)  # 연결 수신 대기 상태(리스닝 수(동시 접속) 설정)
    print('클라이언트 연결 대기')

    # 연결 수락(클라이언트 소켓 주소를 반환)
    conn, addr = s.accept()
    print(addr)  # 클라이언트 주소 출력

    data = b""  # 수신한 데이터를 넣을 변수
    payload_size = struct.calcsize(">L")
    cap = 1
##e
    frame_queue = Queue()
    darknet_image_queue = Queue(maxsize=1)
    detections_queue = Queue(maxsize=1)
    fps_queue = Queue(maxsize=1)

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
    # input_path = str2int(args.input)
##s
##e
    #cap = cv2.VideoCapture(input_path)
#cap = cv2.VideoCapture(frame)

    video_width = int(640)
    video_height = int(480)
    Thread(target=video_capture, args=(frame_queue, darknet_image_queue, data, cap)).start()
    Thread(target=inference, args=(darknet_image_queue, detections_queue, fps_queue,cap)).start()
    Thread(target=drawing, args=(frame_queue, detections_queue, fps_queue,cap)).start()

    sys.exit()
