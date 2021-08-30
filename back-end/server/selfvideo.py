import cv2
from time import sleep
 
capture = cv2.VideoCapture('2.mp4')
 
while capture.isOpened():
    run, frame = capture.read()
    if not run:
        print("quit")
        break
    img = cv2.cvtColor(frame, cv2.IMREAD_COLOR)
    cv2.imshow('video', frame)
    if cv2.waitKey(30) & 0xFF == ord('q'):
        break
 
capture.release()
cv2.destroyAllWindows()
