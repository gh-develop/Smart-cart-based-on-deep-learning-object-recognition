import cv2
import numpy as np
import pymysql
import socket

# Reset SQL and Connect

Food = pymysql.connect(
    user='root',
    passwd='1234',
    host='34.71.138.158',
    db='test0512',
    charset='utf8'
)
cursor = Food.cursor(pymysql.cursors.DictCursor)

sql = "UPDATE Food SET number = '0'"
cursor.execute(sql)
sql = "SELECT * FROM `Food`;"
cursor.execute(sql)
result = cursor.fetchall()
result = list(result)
print(result)

# auther - Jay Shankar Bhatt
# using this code without author's permission other then leaning task is strictly prohibited
# provide the path for testing cofing file and tained model form colab
net = cv2.dnn.readNetFromDarknet("/home/suintyu/Downloads/yolov4-tiny-custom_zero_kancho.cfg",
                                 r"/home/suintyu/Downloads/yolov4-tiny-custom_zero_kancho_best.weights")
# Change here for custom classes for trained model

classes = ['curry', 'rice', 'CupNoodle', 'ChicChoc', 'powerade', 'zerocider', 'kancho']
cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)

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

# socket receive buffer size


def recvall(sock, count):
    buf = b''
    while count:
        newbuf = sock.recv(count)
        if not newbuf:
            return None
        buf += newbuf
        count -= len(newbuf)
    return buf


# server ip, port
TCP_IP = '10.178.0.3'
TCP_PORT = 3389

# TCP connect wait
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print('Socket created')
s.bind((TCP_IP, TCP_PORT))
print('Socket bind complete')
s.listen(True)
print('Socket now listening')

# conn: socket object, addr:socket address
conn, addr = s.accept()

# Processing###
while 1:
    length = recvall(conn, 16)

    if length == None:
        s.close()
        print('Socket connection terminate')
        break
    # image data receive
    stringData = recvall(conn, int(length))
    data = np.fromstring(stringData, dtype='uint8')
    img = cv2.imdecode(data, 1)

    # weight data receive
    length2 = recvall(conn, 8)
    weighData = recvall(conn, int(length2))
    weight = int(weighData)
    # sensor weight print
    print("weight: {}".format(weight))

    ### weight stable process

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

    hight, width, _ = img.shape
    blob = cv2.dnn.blobFromImage(img, 1 / 255, (416, 416), (0, 0, 0), swapRB=True, crop=False)

    net.setInput(blob)

    output_layers_name = net.getUnconnectedOutLayersNames()

    layerOutputs = net.forward(output_layers_name)

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
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
            cv2.putText(img, label + " " + confidence, (x, y + 400), font, 2, color, 2)

            # object detection update to check_detection
            check_detection.add(label)
            if label:
                print("object detection occur: {}".format(label))
                print("detection set: {}".format(check_detection))

        # if weight decrease
        if 'curry' in check_detection and (-210 in check_weight or -220 in check_weight or -230 in check_weight):
            out_cu += 1
        # if weight increse
        elif 'curry' in check_detection and (210 in check_weight or 220 in check_weight or 230 in check_weight or 240 in check_weight):
            in_cu += 1

        elif 'rice' in check_detection and (-200 in check_weight or -210 in check_weight):
            out_ri += 1
        elif 'rice' in check_detection and (200 in check_weight or 210 in check_weight or 220 in check_weight):
            in_ri += 1

        elif 'CupNoodle' in check_detection and (-40 in check_weight or -50 in check_weight or -60 in check_weight):
            out_cup += 1
        elif 'CupNoodle' in check_detection and (40 in check_weight or 50 in check_weight or 60 in check_weight or 70 in check_weight):
            in_cup += 1

        elif 'ChicChoc' in check_detection and (-150 in check_weight or -160 in check_weight or -170 in check_weight):
            out_chic += 1
        elif 'ChicChoc' in check_detection and (150 in check_weight or 160 in check_weight or 170 in check_weight or 180 in check_weight):
            in_chic += 1

        elif 'powerade' in check_detection and (-360 in check_weight or -370 in check_weight or -380 in check_weight):
            out_power += 1
        elif 'powerade' in check_detection and (360 in check_weight or 370 in check_weight or 380 in check_weight or 390 in check_weight):
            in_power += 1

        elif 'zerocider' in check_detection and (-250 in check_weight or -260 in check_weight or -270 in check_weight):
            out_zero += 1
        elif 'zerocider' in check_detection and (250 in check_weight or 260 in check_weight or 270 in check_weight or 280 in check_weight):
            in_zero += 1

        elif 'kancho' in check_detection and (-90 in check_weight or -100 in check_weight or -110 in check_weight):
            out_kan += 1
        elif 'kancho' in check_detection and (90 in check_weight or 100 in check_weight or 110 in check_weight or 120 in check_weight):
            in_kan += 1

        # check out algorithm
        # if 'curry' in check_detection and weight == -100:
        if out_cu >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'curry';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***curry number decrease upload***')
            # initialize
            out_cu = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'rice' in check_detection and weight == -200:
        if out_ri >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'rice';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***rice number decrease upload***')
            out_ri = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'CupNoodle' in check_detection and weight == -300:
        if out_cup >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'CupNoodle';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***cupnoodle number decrease upload***')
            out_cup = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'ChicChoc' in check_detection and weight == -400:
        if out_chic >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'ChicChoc';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***chicchoc number decrease upload***')
            out_chic = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'powerade' in check_detection and weight == -500:
        if out_power >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'powerade';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***powerade number decrease upload***')
            out_power = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'zerocider' in check_detection and weight == -600:
        if out_zero >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'zerocider';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***zerocider number decrease upload***')
            out_zero = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'kancho' in check_detection and weight == -700:
        if out_kan >= 1:
            sql = "UPDATE Food SET number = number-1 WHERE product = 'kancho';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***kancho number decrease upload***')
            out_kan = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # check in algorithm
        # if 'curry' in check_detection and weight == 100:
        if in_cu >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'curry';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***curry number increase upload***')
            in_cu = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'rice' in check_detection and weight == 200:
        if in_ri >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'rice';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***rice number increase upload***')
            in_ri = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'CupNoodle' in check_detection and weight == 300:
        if in_cup >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'CupNoodle';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***cupnoodle number increase upload***')
            in_cup = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'ChicChoc' in check_detection and weight == 400:
        if in_chic >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'ChicChoc';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***chicchoc number increase upload***')
            in_chic = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'powerade' in check_detection and weight == 500:
        if in_power >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'powerade';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***powerade number increase upload***')
            in_power = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'zerocider' in check_detection and weight == 600:
        if in_zero >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'zerocider';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***zerocider number increase upload***')
            in_zero = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

        # elif 'kancho' in check_detection and weight == 700:
        if in_kan >= 1:
            sql = "UPDATE Food SET number = number+1 WHERE product = 'kancho';"
            check_detection = set()  # set \ucd08\uae30\ud654
            print('***kancho number increase upload***')
            in_kan = 0
            check_weight = set()  # set \ucd08\uae30\ud654
            sensor_weight = []
            index = 0
            minus = 0
            cursor.execute(sql)
            Food.commit()

    cv2.imshow('img', img)
    if cv2.waitKey(1) == ord('q'):
        break
cap.release()
cv2.destroyAllWindows()
