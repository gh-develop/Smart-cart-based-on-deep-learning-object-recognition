import socket
import cv2
import pickle
import struct
import numpy as np
import pymysql

# Reset SQL and Connect

USERBASKET = pymysql.connect(
    user='knormal',
    passwd='knormal7',
    host='34.64.244.67',
    db='knormal',
    charset='utf8'
)
cursor = USERBASKET.cursor(pymysql.cursors.DictCursor)

sql = "UPDATE USERBASKET SET classNum = '0'"
cursor.execute(sql)
sql = "SELECT * FROM `USERBASKET`;"
cursor.execute(sql)
USERBASKET.commit()
result = cursor.fetchall()
result = list(result)
print(result)

# auther - Jay Shankar Bhatt
# using this code without author's permission other then leaning task is strictly prohibited
# provide the path for testing cofing file and tained model form colab
net = cv2.dnn.readNetFromDarknet("/home/suintyu/Downloads/yolov4-tiny-custom_zero_kancho.cfg",
                                 r"/home/suintyu/Downloads/yolov4-tiny-custom_zero_kancho_best.weights")
# Change here for custom classes for trained model

classes = ['curry', 'rice', 'CupNoodle',
           'ChicChoc', 'powerade', 'zerocider', 'kancho']

# Initial Parameter###
# object detection
check_detection = set()
# weight detection
check_weight = set()
# sensor_weight
sensor_weight = []

# check in
in_cu = in_ri = in_cup = in_chic = in_power = in_zero = in_kan = 0

# check out
out_cu = out_ri = out_cup = out_chic = out_power = out_zero = out_kan = 0

# sensor_weght index
index = 0
# compare received weight
minus = 0

# weight stabilization variable
# jump on/off variable
jump = 0
# current weight from sensor
current_weight = 0
# past weight from sensor
past_weight = 0

# Connect With Socket###


def recvall(sock, count):
    buf = b''
    while count:
        newbuf = sock.recv(count)
        if not newbuf:
            return None
        buf += newbuf
        count -= len(newbuf)
    return buf


ip = '10.178.0.3'  # ip 주소
port = 3389  # port 번호

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
    print("Frame Size : {}".format(msg_size))  # 프레임 크기 출력

    # 역직렬화(de-serialization) : 직렬화된 파일이나 바이트를 원래의 객체로 복원하는 것
    # 직렬화되어 있는 binary file로 부터 객체로 역직렬화
    frame = pickle.loads(frame_data, fix_imports=True, encoding="bytes")
    frame = cv2.imdecode(frame, cv2.IMREAD_COLOR)  # 프레임 디코딩

    # weight data receive
    #length2 = recvall(conn, 8)
    #weighData = recvall(conn, int(length2))
    weight = int(200)  # int(weighData)
    # sensor weight print
    print("weight: {}".format(weight))

    # weight stable process

    # weight difference detect
    past_weight = current_weight
    current_weight = weight
    weight_difference = current_weight - past_weight

    if abs(weight_difference) > 10 and jump == 0:
        jump = 3

    if jump >= 1:
        jump -= 1
        print("jump process[{}] running".format(jump))
    else:
        sensor_weight.append(weight)
        print("weight append success!: {}".format(sensor_weight))
        # last weight - before last weight
        if len(sensor_weight) > 1:
            # index increase
            index = index + 1
            minus = sensor_weight[index] - sensor_weight[index - 1]

    # weight change occur
    if abs(minus) > 10:
        # ex) round 781 -> 780
        minus = round(minus, -1)
        # change weight update to check_weight
        check_weight.add(minus)
        # change weight print
        print("weight change detection: {}".format(check_weight))

    hight, width, _ = frame.shape
    blob = cv2.dnn.blobFromImage(
        frame, 1 / 255, (416, 416), (0, 0, 0), swapRB=True, crop=False)

    net.setInput(blob)

    output_layers_name = net.getUnconnectedOutLayersNames()

    layerOutputs = net.forward(output_layers_name)

    """
    boxes = []
    confidences = []
    class_ids = []


    for output in layerOutputs:
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
    """
    boxes = []
    confidences = []
    class_ids = []

    for output in layerOutputs:
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
        for i, j in zip(indexes.flatten(), range(500)):
            x, y, w, h = boxes[i]
            label = str(classes[class_ids[i]])
            confidence = str(round(confidences[i], 2))
            color = colors[i]
            cv2.rectangle(frame, (x, y), (x + w, y + h), color, 2)
            cv2.putText(frame, label + " " + confidence,
                        (x, y + 400), font, 2, color, 2)

            # object detection update to check_detection
            check_detection.add(label)
            if label:
                print("object detection occur: {}".format(label))
                print("detection set: {}".format(check_detection))

        # if weight decrease
        if 'curry' in check_detection and (
                -210 in check_weight or -220 in check_weight or -230 in check_weight or -240 in check_weight):
            out_cu += 1
        # if weight increse
        elif 'curry' in check_detection and (
                210 in check_weight or 220 in check_weight or 230 in check_weight or 240 in check_weight):
            in_cu += 1

        elif 'rice' in check_detection and (-200 in check_weight or -210 in check_weight or -220 in check_weight):
            out_ri += 1
        elif 'rice' in check_detection and (200 in check_weight or 210 in check_weight or 220 in check_weight):
            in_ri += 1

        elif 'CupNoodle' in check_detection and (
                -30 in check_weight or -40 in check_weight or -50 in check_weight or -60 in check_weight or -70 in check_weight):
            out_cup += 1
        elif 'CupNoodle' in check_detection and (
                30 in check_weight or 40 in check_weight or 60 in check_weight or 70 in check_weight):
            in_cup += 1

        elif 'ChicChoc' in check_detection and (
                -150 in check_weight or -160 in check_weight or -170 in check_weight or -180 in check_weight):
            out_chic += 1
        elif 'ChicChoc' in check_detection and (
                150 in check_weight or 160 in check_weight or 170 in check_weight or 180 in check_weight):
            in_chic += 1

        elif 'powerade' in check_detection and (
                -360 in check_weight or -370 in check_weight or -380 in check_weight or -390 in check_weight):
            out_power += 1
        elif 'powerade' in check_detection and (
                360 in check_weight or 370 in check_weight or 380 in check_weight or 390 in check_weight):
            in_power += 1

        elif 'zerocider' in check_detection and (
                -250 in check_weight or -260 in check_weight or -270 in check_weight or -280 in check_weight):
            out_zero += 1
        elif 'zerocider' in check_detection and (
                250 in check_weight or 260 in check_weight or 270 in check_weight or 280 in check_weight):
            in_zero += 1

        elif 'kancho' in check_detection and (
                -90 in check_weight or -100 in check_weight or -110 in check_weight or -120 in check_weight):
            out_kan += 1
        elif 'kancho' in check_detection and (
                90 in check_weight or 100 in check_weight or 110 in check_weight or 120 in check_weight):
            in_kan += 1

        # check out algorithm
        # if 'curry' in check_detection and weight == -100:
        if out_cu >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'curry';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***curry number decrease upload***')
            # initialize
            out_cu = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'rice' in check_detection and weight == -200:
        if out_ri >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'rice';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***rice number decrease upload***')
            out_ri = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'CupNoodle' in check_detection and weight == -300:
        if out_cup >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'cupnoodle';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***cupnoodle number decrease upload***')
            out_cup = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'ChicChoc' in check_detection and weight == -400:
        if out_chic >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'chicchoc';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***chicchoc number decrease upload***')
            out_chic = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'powerade' in check_detection and weight == -500:
        if out_power >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'powerade';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***powerade number decrease upload***')
            out_power = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'zerocider' in check_detection and weight == -600:
        if out_zero >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'zerocider';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***zerocider number decrease upload***')
            out_zero = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'kancho' in check_detection and weight == -700:
        if out_kan >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum-1 WHERE className = 'kancho';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***kancho number decrease upload***')
            out_kan = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # check in algorithm
        # if 'curry' in check_detection and weight == 100:
        if in_cu >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'curry';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***curry number increase upload***')
            in_cu = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'rice' in check_detection and weight == 200:
        if in_ri >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'rice';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***rice number increase upload***')
            in_ri = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'CupNoodle' in check_detection and weight == 300:
        if in_cup >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'cupnoodle';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***cupnoodle number increase upload***')
            in_cup = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'ChicChoc' in check_detection and weight == 400:
        if in_chic >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'chicchoc';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***chicchoc number increase upload***')
            in_chic = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'powerade' in check_detection and weight == 500:
        if in_power >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'powerade';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***powerade number increase upload***')
            in_power = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'zerocider' in check_detection and weight == 600:
        if in_zero >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'zerocider';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***zerocider number increase upload***')
            in_zero = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

        # elif 'kancho' in check_detection and weight == 700:
        if in_kan >= 1:
            sql = "UPDATE USERBASKET SET classNum = classNum+1 WHERE className = 'kancho';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***kancho number increase upload***')
            in_kan = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            USERBASKET.commit()

    # 영상 출력
    cv2.imshow('TCP_Frame_Socket', frame)

    # 1초 마다 키 입력 상태를 받음
    if cv2.waitKey(1) == ord('q'):  # q를 입력하면 종료
        break

cv2.destroyAllWindows()
