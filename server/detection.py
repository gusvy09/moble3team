# detection.py
# 감지 여부 전송, 버튼 클릭 여부 받기
from ultralytics import YOLO
from flask import Flask
import threading
import time
from flask_cors import CORS

app = Flask(__name__)
CORS(app)
lock = threading.Lock()

activate = "on"
mode = True  # true : 화재감지 on, False : 화재감지 off

# 클래스 이름
classNames = ["fire", "light"]


class Detection:
    detection_result = False

    @classmethod
    def is_detection(cls):
        global activate
        global mode
        while True:
            Detection.detection_result = False
            if mode:  # 감지 모드가 켜져있을 경우에만 화재 감지 수행
                try:
                    model = YOLO("fire_light.pt")
                    result = model("pic.jpg", show=False)

                    for r in result:
                        boxes = r.boxes
                        for box in boxes:
                            cls = int(box.cls[0])
                            currentClass = classNames[cls]
                            if currentClass == "fire":
                                activate = "on"
                                Detection.detection_result = True
                                time.sleep(0.5)
                                break
                except:
                    pass
            else:
                print("감지off")
            time.sleep(0.1)


# 감지 여부
@app.route("/", methods=["GET", "POST"])
def default():
    with lock:
        detection_result = Detection.detection_result
    return str(detection_result)


# 센서 off
@app.route("/off", methods=["GET", "POST"])
def off():
    global activate
    activate = "off"
    return activate


# 센서 상태
@app.route("/check", methods=["GET", "POST"])
def on():
    global activate
    return activate


# 감지모드 반환
@app.route("/mode", methods=["GET", "POST"])
def is_mode():
    global mode
    return str(mode)


# 감지모드 변경 및 반환
@app.route("/changemode", methods=["GET", "POST"])
def changemode():
    global mode
    mode = not mode
    return str(mode)


if __name__ == "__main__":
    t = threading.Thread(target=Detection.is_detection)
    t.daemon = True
    t.start()
    app.run(host="0.0.0.0", port=, threaded=True)
