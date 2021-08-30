# -*- coding: utf-8 -*-
import socket
import cv2
import pickle
import struct
import numpy as np
import time

prevtime = 0




# auther - Jay Shankar Bhatt
# using this code without author's permission other then leaning task is strictly prohibited
# provide the path for testing cofing file and tained model form colab
net = cv2.dnn.readNetFromDarknet("/home/ubuntu/han/yolov4-tiny-custom_3th.cfg",r"/home/ubuntu/han/yolov4-tiny-custom_3th.weights")
# Change here for custom classes for trained model

classes =[]
with open('obj.names', 'r') as f:
    classes = [line.strip() for line in f.readlines()]

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

while True:
    curTime = time.time()
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

    # 물체인식 시작
   #
   #
    
    hight, width, _ = frame.shape
    blob = cv2.dnn.blobFromImage(frame, 1/255, (416, 416), (0, 0, 0), swapRB=True, crop=False)

    net.setInput(blob)

    output_layers_name = net.getUnconnectedOutLayersNames()

    layerOutputs = net.forward(output_layers_name)

    boxes = []
    confidences = []
    class_ids = []
#
#
    
    for output in layerOutputs:
        print('check point 1')
        for detection in output:
            score = detection[5:]
            class_id = np.argmax(score)
            confidence = score[class_id]
            if confidence > 0.7:
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * hight)
                w = int(detection[2] * width)
                h = int(detection[3] * hight)
                x = int(center_x - w / 2)
                y = int(center_y - h / 2)
                boxes.append([x, y, w, h])
                confidences.append((float(confidence)))
                class_ids.append(class_id)
    
    indexes = cv2.dnn.NMSBoxes(boxes, confidences, .5, .4)
    boxes = []
    confidences = []
    class_ids = []



    for output in layerOutputs:
        print('check point 2')
        for detection in output:
            score = detection[5:]
            class_id = np.argmax(score)
            confidence = score[class_id]
            if confidence > 0.5:
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * hight)
                w = int(detection[2] * width)
                h = int(detection[3] * hight)

                x = int(center_x - w / 2)
                y = int(center_y - h / 2)

                boxes.append([x, y, w, h])
                confidences.append((float(confidence)))
                class_ids.append(class_id)

    indexes = cv2.dnn.NMSBoxes(boxes, confidences, .8, .4)
    font = cv2.FONT_HERSHEY_PLAIN
    colors = np.random.uniform(0, 255, size=(len(boxes), 3))
    
    if len(indexes) > 0:
        print('check point 3')
        for i, j in zip(indexes.flatten(), range(500)):
            x, y, w, h = boxes[i]
            label = str(classes[class_ids[i]])
            confidence = str(round(confidences[i], 2))
            color = colors[i]
            cv2.rectangle(frame, (x, y), (x + w, y + h), color, 2)
            cv2.putText(frame, label + " " + confidence, (x, y), font, 2, color, 2)

    # 영상 출력
    
    cv2.imshow('TCP_Frame_Socket', frame)   
    sec = time.time() - curTime
    print('second',sec)

    # 1초 마다 키 입력 상태를 받음
    if cv2.waitKey(1) == ord('q'):  # q를 입력하면 종료
        break
cv2.destroyAllWindows()
