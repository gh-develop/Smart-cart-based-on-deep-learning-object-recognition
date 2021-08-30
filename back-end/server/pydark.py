import numpy as np
import cv2
import pyyolo
cap = cv2.VideoCapture('1.mp4')
meta_filepath = "/home/ubuntu/han/darknet/obj.data"
cfg_filepath = "/home/ubuntu/han/darknet/yolov4-tiny-custom_3th.cfg"
weights_filepath = "/home/ubuntu/han/darknet/yolov4-tiny-custom_3th.weights"
meta = pyyolo.load_meta(meta_filepath)
net = pyyolo.load_net(cfg_filepath, weights_filepath, False)
while(cap.isOpened()):
    ret, frame = cap.read()
    if not ret:
        break
    yolo_img = pyyolo.array_to_image(frame)
    res = pyyolo.detect(net, meta, yolo_img)
    for r in res:
        cv2.rectangle(frame, r.bbox.get_point(pyyolo.BBox.Location.TOP_LEFT, is_int=True),
                      r.bbox.get_point(pyyolo.BBox.Location.BOTTOM_RIGHT, is_int=True), (0, 255, 0), 2)
    cv2.imshow('frame', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break
cap.release()
cv2.destroyAllWindows()
 
