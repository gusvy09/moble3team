# sensor.py
# 감지 여부 받아서 센서 제어
import RPi.GPIO as GPIO
import requests
import time

server_ip = ""
local_ip = ""
led_pin = 18
speaker_pin = 23
servo_pin = 25

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

GPIO.setup(led_pin, GPIO.OUT)
GPIO.setup(speaker_pin, GPIO.OUT)
GPIO.setup(servo_pin, GPIO.OUT)

GPIO.output(led_pin, GPIO.LOW)

p = GPIO.PWM(speaker_pin, 1)
w = GPIO.PWM(servo_pin, 50)
w.start(2.5)

time_event = 0


# 센서 켜기
def on():
    GPIO.output(led_pin, GPIO.HIGH)
    p.start(50)
    w.start(12.5)


# 센서 끄기
def off():
    GPIO.output(led_pin, GPIO.LOW)
    p.stop()


# 센서 제어
def test():
    global time_event
    time_event = 0
    while True:
        time_event = time_event + 1
        response = requests.get(f"http://{server_ip}:/check")
        activate = response.text
        # 시간이 지나거나, 버튼 클릭으로 off를 받았을 때 종료
        if time_event > 30 or activate == "off":
            off()
            break
        time.sleep(0.1)


# 감지 여부
while True:
    response = requests.get(f"http://{server_ip}:/")
    detection = response.text
    print(detection)
    # True가 들어오면 off가 들어오는지 확인
    if detection == "True":
        on()
        detection = "False"
        test()
    time.sleep(0.1)
